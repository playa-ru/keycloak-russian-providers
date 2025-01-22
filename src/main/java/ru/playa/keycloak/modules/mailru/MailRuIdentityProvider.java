package ru.playa.keycloak.modules.mailru;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.Urls;
import ru.playa.keycloak.modules.Utils;

import java.io.IOException;
import ru.playa.keycloak.modules.exception.MissingEmailException;


/**
 * Провайдер OAuth-авторизации через <a href="https://my.mail.ru">Мой Мир</a>.
 * <a href="https://api.mail.ru/docs/guides/oauth/">Подробнее</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class MailRuIdentityProvider
    extends AbstractOAuth2IdentityProvider<MailRuIdentityProviderConfig>
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
    public MailRuIdentityProvider(final KeycloakSession session, final MailRuIdentityProviderConfig config) {
        super(session, config);
        config.setAuthorizationUrl(AUTH_URL);
        config.setTokenUrl(TOKEN_URL);
        config.setUserInfoUrl(PROFILE_URL);
    }

    @Override
    public Object callback(final RealmModel realm, final AuthenticationCallback callback, final EventBuilder event) {
        return new MailRuEndpoint(callback, realm, event, this);
    }

    @Override
    protected boolean supportsExternalExchange() {
        return true;
    }

    @Override
    protected String getProfileEndpointForValidation(final EventBuilder event) {
        return PROFILE_URL;
    }

    @Override
    protected SimpleHttp buildUserInfoRequest(final String subjectToken, final String userInfoUrl) {
        logger.info("subjectToken: " + subjectToken);
        logger.info("userInfoUrl: " + userInfoUrl);

        return SimpleHttp.doGet(PROFILE_URL + "?access_token=" + subjectToken, session);
    }

    @Override
    protected BrokeredIdentityContext extractIdentityFromProfile(final EventBuilder event, final JsonNode profile) {
        logger.info("profile: " + profile.toString());

        BrokeredIdentityContext user = new BrokeredIdentityContext(getJsonProperty(profile, "email"), getConfig());

        String email = getJsonProperty(profile, "email");

        if (Utils.isNullOrEmpty(email)) {
            throw new MissingEmailException(MailRuIdentityProviderFactory.PROVIDER_ID);
        } else {
            Utils.isHostedDomain(email, getConfig().getHostedDomain(), MailRuIdentityProviderFactory.PROVIDER_ID);
        }

        user.setEmail(email);
        user.setUsername(email);
        user.setFirstName(getJsonProperty(profile, "first_name"));
        user.setLastName(getJsonProperty(profile, "last_name"));

        user.setIdp(this);

        AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, profile, getConfig().getAlias());

        return user;
    }

    @Override
    protected BrokeredIdentityContext doGetFederatedIdentity(final String accessToken) {
        try {
            return extractIdentityFromProfile(
                null,
                SimpleHttp.doGet(PROFILE_URL + "?access_token=" + accessToken, session).asJson()
            );
        } catch (IOException e) {
            throw new IdentityBrokerException("Could not obtain user profile from MailRu: " + e.getMessage(), e);
        }
    }

    @Override
    protected String getDefaultScopes() {
        return DEFAULT_SCOPE;
    }

    /**
     * Переопределение метода для получения токена.
     */
    protected static class MailRuEndpoint extends Endpoint {

        /**
         * Necessary evil, super class has private field :(.
         * Необходимое зло, у суперкласса есть приватное поле :(.
         */
        private final MailRuIdentityProvider providerOverride;

        /**
         * @param callback Авторизационный коллбэк.
         * @param realm реалм.
         * @param event эвент.
         * @param provider провайдер.
         */
        public MailRuEndpoint(
            final AuthenticationCallback callback,
            final RealmModel realm,
            final EventBuilder event,
            final MailRuIdentityProvider provider
        ) {
            super(callback, realm, event, provider);
            this.providerOverride = provider;
        }

        @Override
        public SimpleHttp generateTokenRequest(final String authorizationCode) {
            String clientID = providerOverride.getConfig().getClientId();
            String clientSecret = providerOverride.getConfig().getClientSecret();
            String credentials = Base64
                .getEncoder()
                .encodeToString((clientID + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

            return SimpleHttp
                .doPost(providerOverride.getConfig().getTokenUrl(), session)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", "Basic " + credentials)
                .param(OAUTH2_PARAMETER_CODE, authorizationCode)
                .param(OAUTH2_PARAMETER_REDIRECT_URI, Urls
                    .identityProviderAuthnResponse(
                        session.getContext().getUri().getBaseUri(),
                        providerOverride.getConfig().getAlias(),
                        session.getContext().getRealm().getName()
                    )
                    .toString())
                .param(OAUTH2_PARAMETER_GRANT_TYPE, OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE);
        }
    }

}