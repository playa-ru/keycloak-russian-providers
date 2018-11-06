package ru.playa.keycloak.modules;

import org.keycloak.OAuth2Constants;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.common.ClientConnection;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.ErrorPage;
import org.keycloak.services.messages.Messages;

import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

/**
 * Базовый провайдер OAuth-авторизации для российских социальных сетей.
 *
 * @author Anatoliy Pokhresnyi
 */
public abstract class AbstractRussianOAuth2IdentityProvider<C extends OAuth2IdentityProviderConfig>
        extends AbstractOAuth2IdentityProvider<C> {

    /**
     * Создает объект OAuth-авторизации для российских социальных сейтей.
     *
     * @param session Сессия Keycloak.
     * @param config  Конфигурация OAuth-авторизации.
     */
    public AbstractRussianOAuth2IdentityProvider(KeycloakSession session, C config) {
        super(session, config);
    }

    @Override
    public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
        return new Endpoint(callback, realm, event);
    }

    /**
     * Переопределенный класс {@link AbstractOAuth2IdentityProvider.Endpoint}.
     * Класс переопределен с целью возвращения человеко-читаемой ошибки если
     * в профиле социальной сети не указана электронная почта.
     */
    protected class Endpoint {

        private AuthenticationCallback callback;
        private RealmModel realm;
        private EventBuilder event;

        @Context
        private KeycloakSession session;

        @Context
        private ClientConnection clientConnection;

        @Context
        private HttpHeaders headers;

        public Endpoint(AuthenticationCallback aCallback, RealmModel
                aRealm, EventBuilder aEvent) {
            this.callback = aCallback;
            this.realm = aRealm;
            this.event = aEvent;
        }

        @GET
        public Response authResponse(
                @QueryParam(AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_STATE) String state,
                @QueryParam(AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_CODE) String authorizationCode,
                @QueryParam(OAuth2Constants.ERROR) String error) {
            if (error != null) {
                //logger.error("Failed " + getConfig().getAlias() + " broker login: " + error);
                if (error.equals(ACCESS_DENIED)) {
                    logger.error(ACCESS_DENIED + " for broker login " + getConfig().getProviderId());
                    return callback.cancelled(state);
                } else {
                    logger.error(error + " for broker login " + getConfig().getProviderId());
                    return callback.error(state, Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
                }
            }

            try {

                if (authorizationCode != null) {
                    String response = generateTokenRequest(authorizationCode).asString();

                    BrokeredIdentityContext federatedIdentity = getFederatedIdentity(response);

                    if (getConfig().isStoreToken() && federatedIdentity.getToken() == null) {
                        // make sure that token wasn't already set by getFederatedIdentity();
                        // want to be able to allow provider to set the token itself.
                        federatedIdentity.setToken(response);

                    }

                    federatedIdentity.setIdpConfig(getConfig());
                    federatedIdentity.setIdp(AbstractRussianOAuth2IdentityProvider.this);
                    federatedIdentity.setCode(state);

                    return callback.authenticated(federatedIdentity);
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
                    .param(OAUTH2_PARAMETER_REDIRECT_URI, session.getContext().getUri().getAbsolutePath().toString())
                    .param(OAUTH2_PARAMETER_GRANT_TYPE, OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE);
        }
    }
}
