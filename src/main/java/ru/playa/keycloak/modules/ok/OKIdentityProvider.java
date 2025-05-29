package ru.playa.keycloak.modules.ok;

import com.fasterxml.jackson.databind.JsonNode;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import ru.playa.keycloak.modules.Utils;

import java.io.IOException;
import ru.playa.keycloak.exception.MissingEmailException;

/**
 * Провайдер OAuth-авторизации через <a href="https://ok.ru/">Одноклассники</a>.
 * <a href="https://apiok.ru/ext/oauth/">Подробнее</a>.
 *
 * @author Anatoliy Pokhresnyi
 * @author dmitrymalinin
 */
public class OKIdentityProvider
    extends AbstractOAuth2IdentityProvider<OKIdentityProviderConfig>
    implements SocialIdentityProvider<OKIdentityProviderConfig> {

    /**
     * Запрос кода подтверждения.
     */
    private static final String AUTH_URL = "https://connect.ok.ru/oauth/authorize";

    /**
     * Обмен кода подтверждения на токен.
     */
    private static final String TOKEN_URL = "https://api.ok.ru/oauth/token.do";

    /**
     * Запрос информации о пользователе.
     */
    private static final String PROFILE_URL = "https://api.ok.ru/fb.do";

    /**
     * Права доступа к данным пользователя по умолчанию.
     */
    private static final String DEFAULT_SCOPE = "";

    /**
     * Создает объект OAuth-авторизации через
     * <a href="https://ok.ru/">Одноклассники</a>.
     *
     * @param session Сессия Keycloak.
     * @param config  Конфигурация OAuth-авторизации.
     */
    public OKIdentityProvider(final KeycloakSession session, final OKIdentityProviderConfig config) {
        super(session, config);
        config.setAuthorizationUrl(AUTH_URL);
        config.setTokenUrl(TOKEN_URL);
        config.setUserInfoUrl(PROFILE_URL);
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
        return SimpleHttp.doGet(PROFILE_URL + "?access_token=" + subjectToken, session);
    }

    @Override
    protected BrokeredIdentityContext extractIdentityFromProfile(final EventBuilder event, final JsonNode profile) {
        logger.info("profile: " + profile.toString());

        BrokeredIdentityContext user = new BrokeredIdentityContext(getJsonProperty(profile, "uid"), getConfig());

        String email = getJsonProperty(profile, "email");
        if (getConfig().isEmailRequired() && Utils.isNullOrEmpty(email)) {
            throw new MissingEmailException(OKIdentityProviderFactory.PROVIDER_ID);
        }

        String username = getJsonProperty(profile, "login");

        if (Utils.nonNullOrEmpty(email)) {
            user.setUsername(email);
        } else {
            if (Utils.nonNullOrEmpty(username)) {
                user.setUsername(username);
            } else {
                user.setUsername("ok." + user.getId());
            }
        }

        user.setEmail(email);
        user.setFirstName(getJsonProperty(profile, "first_name"));
        user.setLastName(getJsonProperty(profile, "last_name"));

        user.setIdp(this);

        AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, profile, getConfig().getAlias());

        return user;
    }

    @Override
    protected BrokeredIdentityContext doGetFederatedIdentity(final String accessToken) {
        try {
            String params = "application_key="
                + getConfig().getPublicKey()
                + "format=jsonmethod=users.getCurrentUser"
                + Utils.hex(Utils.md5(accessToken + getConfig().getClientSecret()));

            String url = PROFILE_URL
                + "?application_key=" + getConfig().getPublicKey()
                + "&format=json"
                + "&method=users.getCurrentUser"
                + "&sig=" + Utils.hex(Utils.md5(params))
                + "&access_token=" + accessToken;

            logger.info("url: " + url);

            return extractIdentityFromProfile(null, SimpleHttp.doGet(url, session).asJson());
        } catch (IOException e) {
            throw new IdentityBrokerException("Could not obtain user profile from OK: " + e.getMessage(), e);
        }
    }

    @Override
    protected String getDefaultScopes() {
        return DEFAULT_SCOPE;
    }
}