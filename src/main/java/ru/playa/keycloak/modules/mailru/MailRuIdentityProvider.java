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
import ru.playa.keycloak.modules.AbstractRussianOAuth2IdentityProvider;
import ru.playa.keycloak.modules.Utils;

import java.io.IOException;

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
    public MailRuIdentityProvider(final KeycloakSession session, final MailRuIdentityProviderConfig config) {
        super(session, config);
        config.setAuthorizationUrl(AUTH_URL);
        config.setTokenUrl(TOKEN_URL);
        config.setUserInfoUrl(PROFILE_URL);
    }

    @Override
    public Object callback(final RealmModel realm, final AuthenticationCallback callback, final EventBuilder event) {
        return new MailRuEndpoint(callback, event, this, session);
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

        BrokeredIdentityContext user = new BrokeredIdentityContext(getJsonProperty(profile, "email"));

        String email = getJsonProperty(profile, "email");

        if (Utils.isNullOrEmpty(email)) {
            throw new IllegalArgumentException(Utils.toEmailErrorMessage("MailRu"));
        } else {
            Utils.isHostedDomain(email, getConfig().getHostedDomain(), "MailRu");
        }

        user.setEmail(email);
        user.setUsername(email);
        user.setFirstName(getJsonProperty(profile, "first_name"));
        user.setLastName(getJsonProperty(profile, "last_name"));

        user.setIdp(this);
        user.setIdpConfig(getConfig());

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


}