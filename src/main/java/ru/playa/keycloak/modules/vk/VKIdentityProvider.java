package ru.playa.keycloak.modules.vk;

import com.fasterxml.jackson.databind.JsonNode;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import ru.playa.keycloak.modules.AbstractRussianOAuth2IdentityProvider;
import ru.playa.keycloak.modules.JsonUtils;
import ru.playa.keycloak.modules.MessageUtils;
import ru.playa.keycloak.modules.StringUtils;

import java.io.IOException;

/**
 * Провайдер OAuth-авторизации через <a href="https://vk.com">ВКонтакте</a>.
 * <a href="https://vk.com/dev/access_token">Подробнее</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class VKIdentityProvider
        extends AbstractRussianOAuth2IdentityProvider<VKIdentityProviderConfig>
        implements SocialIdentityProvider<VKIdentityProviderConfig> {

    /**
     * Запрос кода подтверждения.
     */
    private static final String AUTH_URL = "https://oauth.vk.com/authorize";

    /**
     * Обмен кода подтверждения на токен.
     */
    private static final String TOKEN_URL = "https://oauth.vk.com/access_token";

    /**
     * Запрос информации о пользователе.
     */
    private static final String PROFILE_URL = "https://api.vk.com/method/users.get";

    /**
     * Права доступа к данным пользователя по умолчанию.
     */
    private static final String DEFAULT_SCOPE = "email";

    /**
     * Создает объект OAuth-авторизации через
     * <a href="https://vk.com">ВКонтакте</a>.
     *
     * @param session Сессия Keycloak.
     * @param config  Конфигурация OAuth-авторизации.
     */
    public VKIdentityProvider(KeycloakSession session, VKIdentityProviderConfig config) {
        super(session, config);
        config.setAuthorizationUrl(AUTH_URL + "?v=" + getConfig().getVersion());
        config.setTokenUrl(TOKEN_URL + "?v=" + getConfig().getVersion());
        config.setUserInfoUrl(PROFILE_URL + "?v=" + getConfig().getVersion());
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
        return SimpleHttp.doGet(PROFILE_URL
                + "?v=" + getConfig().getVersion()
                + "&access_token=" + subjectToken, session);
    }

    @Override
    protected BrokeredIdentityContext extractIdentityFromProfile(EventBuilder event, JsonNode node) {
        JsonNode context = node.get("response").get(0);
        BrokeredIdentityContext user = new BrokeredIdentityContext(getJsonProperty(context, "id"));

        user.setUsername(getJsonProperty(context, "screen_name"));
        user.setFirstName(getJsonProperty(context, "first_name"));
        user.setLastName(getJsonProperty(context, "last_name"));

        user.setIdpConfig(getConfig());
        user.setIdp(this);

        AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, context, getConfig().getAlias());

        return user;
    }

    private BrokeredIdentityContext extractIdentityFromProfile(JsonNode node, String email) {
        BrokeredIdentityContext user = extractIdentityFromProfile(null, node);

        if (StringUtils.isNullOrEmpty(email)) {
            throw new IllegalArgumentException(MessageUtils.email("VK"));
        }

        if (StringUtils.isNullOrEmpty(user.getUsername())) {
            user.setUsername(email);
        }

        user.setEmail(email);

        return user;
    }

    @Override
    public BrokeredIdentityContext getFederatedIdentity(String response) {
        String accessToken = extractTokenFromResponse(response, getAccessTokenResponseParameter());
        String userId = JsonUtils.getAsString(response, "user_id");
        String email = JsonUtils.getAsString(response, "email");

        if (accessToken == null) {
            throw new IdentityBrokerException("No access token available in OAuth server response: " + response);
        }

        BrokeredIdentityContext context = doGetFederatedIdentity(accessToken, userId, email);
        context.getContextData().put(FEDERATED_ACCESS_TOKEN, accessToken);
        return context;
    }

    /**
     * Запрос информации о пользователе.
     *
     * @return Данные авторизованного пользователя.
     */
    private BrokeredIdentityContext doGetFederatedIdentity(String accessToken, String userId, String email) {
        try {
            String url = PROFILE_URL
                    + "?v=" + getConfig().getVersion()
                    + "&access_token=" + accessToken
                    + "&user_ids=" + userId
                    + "&fields=screen_name&name_case=Nom";

            return extractIdentityFromProfile(
                    SimpleHttp
                            .doGet(url, session)
                            .param("content-type", "application/json; charset=utf-8")
                            .asJson(),
                    email);
        } catch (IOException e) {
            throw new IdentityBrokerException("Could not obtain user profile from VK: " + e.getMessage(), e);
        }
    }

    @Override
    protected BrokeredIdentityContext doGetFederatedIdentity(String accessToken) {
        try {
            String url = PROFILE_URL
                    + "?v=" + getConfig().getVersion()
                    + "&access_token=" + accessToken;
            return extractIdentityFromProfile(null, SimpleHttp.doGet(url, session).asJson());
        } catch (IOException e) {
            throw new IdentityBrokerException("Could not obtain user profile from VK: " + e.getMessage(), e);
        }
    }

    @Override
    protected String getDefaultScopes() {
        return DEFAULT_SCOPE;
    }
}