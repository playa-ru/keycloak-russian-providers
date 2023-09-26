package ru.playa.keycloak.modules;

import org.keycloak.OAuth2Constants;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.ErrorPage;
import org.keycloak.services.Urls;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * Базовый провайдер OAuth-авторизации для российских социальных сетей.
 *
 * @param <C> Тип объекта настроек.
 * @author Anatoliy Pokhresnyi
 */
public abstract class AbstractRussianOAuth2IdentityProvider<C extends OAuth2IdentityProviderConfig>
        extends AbstractOAuth2IdentityProvider<C> {

    private final String providerID;

    /**
     * Создает объект OAuth-авторизации для российских социальных сейтей.
     *
     * @param session Сессия Keycloak.
     * @param config  Конфигурация OAuth-авторизации.
     */
    public AbstractRussianOAuth2IdentityProvider(KeycloakSession session, C config, String aProviderID) {
        super(session, config);

        this.providerID = aProviderID;

        logger.infof("Provider %s Config %s", providerID, config.getConfig());
    }

    @Override
    public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
        return new Endpoint(callback, realm, event, this.session, this);
    }

    /**
     * Переопределенный класс {@link AbstractOAuth2IdentityProvider.Endpoint}.
     * Класс переопределен с целью возвращения человеко-читаемой ошибки если
     * в профиле социальной сети не указана электронная почта.
     */
    protected class Endpoint {

        private final AbstractOAuth2IdentityProvider provider;
        private final AuthenticationCallback callback;
        private final RealmModel realm;
        private final EventBuilder event;
        private final KeycloakSession session;

        public Endpoint(
                AuthenticationCallback aCallback,
                RealmModel aRealm,
                EventBuilder aEvent,
                KeycloakSession aSession,
                AbstractOAuth2IdentityProvider aProvider
        ) {
            this.callback = aCallback;
            this.realm = aRealm;
            this.event = aEvent;
            this.session = aSession;
            this.provider = aProvider;
        }

        @GET
        public Response authResponse(
                @QueryParam(AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_STATE) String state,
                @QueryParam(AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_CODE) String authorizationCode,
                @QueryParam(OAuth2Constants.ERROR) String error) {
            logger.infof(
                    "Endpoint AuthResponse. State: %s. Code: %s. Error %s", state, authorizationCode, error
            );

            if (error != null) {
                if (error.equals(ACCESS_DENIED)) {
                    logger.error(
                            "ACCESS_DENIED: " + ACCESS_DENIED + " for broker login " + getConfig().getProviderId()
                    );
                    OAuth2IdentityProviderConfig providerConfig = this.provider.getConfig();

                    return callback.cancelled(providerConfig);
                } else {
                    logger.error("ERROR:" + error + " for broker login " + getConfig().getProviderId());
                    return callback.error(Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
                }
            }

            try {
                AuthenticationSessionModel authSession = this.callback.getAndVerifyAuthenticationSession(state);
                this.session.getContext().setAuthenticationSession(authSession);

                logger.info("Authentication session is set");

                if (authorizationCode != null) {
                    String response = this.generateTokenRequest(authorizationCode).asString();

                    logger.infof("Get token. Response %s", response);

                    BrokeredIdentityContext federatedIdentity = getFederatedIdentity(response);
                    if (getConfig().isStoreToken() && federatedIdentity.getToken() == null) {
                        federatedIdentity.setToken(response);
                    }

                    federatedIdentity.setIdpConfig(getConfig());
                    federatedIdentity.setIdp(AbstractRussianOAuth2IdentityProvider.this);
                    federatedIdentity.setAuthenticationSession(authSession);

                    return this.callback.authenticated(federatedIdentity);
                }
            } catch (WebApplicationException e) {
                return e.getResponse();
            } catch (IllegalArgumentException e) {
                logger.error("Failed to make identity provider oauth callback illegal argument exception", e);

                event.event(EventType.LOGIN);
                event.error(Errors.IDENTITY_PROVIDER_LOGIN_FAILURE);
                return ErrorPage.error(session, null,
                        Response.Status.BAD_GATEWAY,
                        MessageUtils.EMAIL);
            } catch (Exception e) {
                logger.error("Failed to make identity provider oauth callback", e);
            }

            event.event(EventType.LOGIN);
            event.error(Errors.IDENTITY_PROVIDER_LOGIN_FAILURE);
            return ErrorPage.error(
                    session,
                    null,
                    Response.Status.BAD_GATEWAY,
                    Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
        }

        public SimpleHttp generateTokenRequest(String authorizationCode) {
            return SimpleHttp.doPost(getConfig().getTokenUrl(), session)
                    .param(OAUTH2_PARAMETER_CODE, authorizationCode)
                    .param(OAUTH2_PARAMETER_CLIENT_ID, getConfig().getClientId())
                    .param(OAUTH2_PARAMETER_CLIENT_SECRET, getConfig().getClientSecret())
                    .param(OAUTH2_PARAMETER_REDIRECT_URI, getRedirectURI())
                    .param(OAUTH2_PARAMETER_GRANT_TYPE, OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE);
        }

        protected String getRedirectURI() {
            return Urls
                    .identityProviderAuthnResponse(
                            session.getContext().getUri().getBaseUri(),
                            providerID,
                            realm.getName()
                    )
                    .toString();
        }
    }
}
