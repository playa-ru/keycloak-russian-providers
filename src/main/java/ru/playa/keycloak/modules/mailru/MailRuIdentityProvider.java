package ru.playa.keycloak.modules.mailru;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.keycloak.OAuth2Constants;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.ErrorPage;
import org.keycloak.services.Urls;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.playa.keycloak.modules.AbstractRussianOAuth2IdentityProvider;
import ru.playa.keycloak.modules.HostedDomainUtils;
import ru.playa.keycloak.modules.MessageUtils;
import ru.playa.keycloak.modules.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Провайдер OAuth-авторизации через <a href="https://my.mail.ru">Мой Мир</a>.
 * <a href="https://api.mail.ru/docs/guides/oauth/">Подробнее</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class MailRuIdentityProvider
        extends AbstractRussianOAuth2IdentityProvider<MailRuIdentityProviderConfig>
        implements SocialIdentityProvider<MailRuIdentityProviderConfig> {

    /**
     * Запрос кода подтверждения.
     */
    private static final String AUTH_URL = "https://oauth.mail.ru/login";

    /**
     * Обмен кода подтверждения на токен.
     */
    private static final String TOKEN_URL = "https://oauth.mail.ru/token";

    /**
     * Запрос информации о пользователе.
     */
    private static final String PROFILE_URL = "https://oauth.mail.ru/userinfo";

    /**
     * Права доступа к данным пользователя по умолчанию.
     */
    private static final String DEFAULT_SCOPE = "userinfo";

    /**
     * Создает объект OAuth-авторизации через
     * <a href="https://my.mail.ru">Мой Мир</a>.
     *
     * @param session Сессия Keycloak.
     * @param config  Конфигурация OAuth-авторизации.
     */
    public MailRuIdentityProvider(KeycloakSession session, MailRuIdentityProviderConfig config) {
        super(session, config, MailRuIdentityProviderFactory.PROVIDER_ID);
        config.setAuthorizationUrl(AUTH_URL);
        config.setTokenUrl(TOKEN_URL);
        config.setUserInfoUrl(PROFILE_URL);
    }

    @Override
    public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
        return new Endpoint(
            callback, realm, event, session, this, MailRuIdentityProviderFactory.PROVIDER_ID
        );
    }

    @Override
    protected boolean supportsExternalExchange() {
        return true;
    }

    @Override
    protected String getProfileEndpointForValidation(EventBuilder event) {
        return PROFILE_URL;
    }

    @Override
    protected SimpleHttp buildUserInfoRequest(String subjectToken, String userInfoUrl) {
        logger.info("subjectToken: " + subjectToken);
        logger.info("userInfoUrl: " + userInfoUrl);

        return SimpleHttp.doGet(PROFILE_URL + "?access_token=" + subjectToken, session);
    }

    @Override
    protected BrokeredIdentityContext extractIdentityFromProfile(EventBuilder event, JsonNode profile) {
        logger.info("profile: " + profile.toString());

        BrokeredIdentityContext user = new BrokeredIdentityContext(getJsonProperty(profile, "email"));

        String email = getJsonProperty(profile, "email");

        if (StringUtils.isNullOrEmpty(email)) {
            throw new IllegalArgumentException(MessageUtils.email("MailRu"));
        } else {
            HostedDomainUtils.isHostedDomain(email, getConfig().getHostedDomain(), "MailRu");
        }

        user.setEmail(email);
        user.setUsername(email);
        user.setFirstName(getJsonProperty(profile, "first_name"));
        user.setLastName(getJsonProperty(profile, "last_name"));

        user.setIdpConfig(getConfig());
        user.setIdp(this);

        AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, profile, getConfig().getAlias());

        return user;
    }

    @Override
    protected BrokeredIdentityContext doGetFederatedIdentity(String accessToken) {
        try {
            return extractIdentityFromProfile(null,
                                              SimpleHttp.doGet(PROFILE_URL + "?access_token=" + accessToken, session)
                                                        .asJson());
        } catch (IOException e) {
            throw new IdentityBrokerException("Could not obtain user profile from MailRu: " + e.getMessage(), e);
        }
    }

    @Override
    protected String getDefaultScopes() {
        return DEFAULT_SCOPE;
    }

    /**
     * Переопределенный класс {@link AbstractRussianOAuth2IdentityProvider.Endpoint}.
     * Класс переопределен с целью изменения логики замены кода на токен.
     */
    protected class Endpoint {

        private final AbstractOAuth2IdentityProvider provider;
        private final AuthenticationCallback callback;
        private final RealmModel realm;
        private final EventBuilder event;
        private final KeycloakSession session;
        private final String providerID;

        public Endpoint(
                AuthenticationCallback aCallback,
                RealmModel aRealm,
                EventBuilder aEvent,
                KeycloakSession aSession,
                AbstractOAuth2IdentityProvider aProvider,
                String aProviderID
        ) {
            this.callback = aCallback;
            this.realm = aRealm;
            this.event = aEvent;
            this.session = aSession;
            this.provider = aProvider;
            this.providerID = aProviderID;
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
                            "ACCESS_DENIED: " + ACCESS_DENIED + " for broker login "
                                    + provider.getConfig().getProviderId()
                    );
                    OAuth2IdentityProviderConfig providerConfig = this.provider.getConfig();

                    return callback.cancelled(providerConfig);
                } else {
                    logger.error("ERROR:" + error + " for broker login " + provider.getConfig().getProviderId());
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

                    BrokeredIdentityContext federatedIdentity = provider.getFederatedIdentity(response);
                    if (provider.getConfig().isStoreToken() && federatedIdentity.getToken() == null) {
                        federatedIdentity.setToken(response);
                    }

                    federatedIdentity.setIdpConfig(provider.getConfig());
                    federatedIdentity.setIdp(provider);
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

        protected String getRedirectURI() {
            return Urls
                    .identityProviderAuthnResponse(
                            session.getContext().getUri().getBaseUri(),
                            providerID,
                            realm.getName()
                    )
                    .toString();
        }

        public SimpleHttp generateTokenRequest(String authorizationCode) {
            String credentials = Base64.getEncoder().encodeToString(
                    (getConfig().getClientId() + ":" + getConfig().getClientSecret()).getBytes(StandardCharsets.UTF_8)
            );

            return SimpleHttp
                    .doPost(getConfig().getTokenUrl(), session)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Authorization", "Basic " + credentials)
                    .param(OAUTH2_PARAMETER_CODE, authorizationCode)
                    .param(OAUTH2_PARAMETER_REDIRECT_URI, getRedirectURI())
                    .param(OAUTH2_PARAMETER_GRANT_TYPE, OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE);
        }
    }
}