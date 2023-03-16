package ru.playa.keycloak.modules.mailru;

import com.fasterxml.jackson.databind.JsonNode;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.Urls;
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
        super(session, config);
        config.setAuthorizationUrl(AUTH_URL);
        config.setTokenUrl(TOKEN_URL);
        config.setUserInfoUrl(PROFILE_URL);
    }

    @Override
    public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
        return new Endpoint(callback, realm, event, this.session);
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
     * Переопределенный класс {@link AbstractOAuth2IdentityProvider.Endpoint}.
     * Класс переопределен с целью возвращения человеко-читаемой ошибки если
     * в профиле социальной сети не указана электронная почта.
     */
    protected class Endpoint extends AbstractRussianOAuth2IdentityProvider<MailRuIdentityProviderConfig>.Endpoint {

        public Endpoint(
                AuthenticationCallback aCallback,
                RealmModel aRealm,
                EventBuilder aEvent,
                KeycloakSession aSession
        ) {
            super(aCallback, aRealm, aEvent, aSession);
        }

        @Override
        public SimpleHttp generateTokenRequest(String authorizationCode) {
            String credentials = Base64.getEncoder().encodeToString(
                    (getConfig().getClientId() + ":" + getConfig().getClientSecret()).getBytes(StandardCharsets.UTF_8)
            );

            String redirectURI = Urls
                    .identityProviderAuthnResponse(
                            getSession().getContext().getUri().getBaseUri(),
                            MailRuIdentityProviderFactory.PROVIDER_ID,
                            getRealm().getName()
                    )
                    .toString();

            return SimpleHttp
                    .doPost(getConfig().getTokenUrl(), session)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Authorization", "Basic " + credentials)
                    .param(OAUTH2_PARAMETER_CODE, authorizationCode)
                    .param(OAUTH2_PARAMETER_REDIRECT_URI, redirectURI)
                    .param(OAUTH2_PARAMETER_GRANT_TYPE, OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE);
        }
    }
}