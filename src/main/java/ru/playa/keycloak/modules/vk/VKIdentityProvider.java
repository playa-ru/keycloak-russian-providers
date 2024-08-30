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
import ru.playa.keycloak.modules.StringUtils;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * Провайдер OAuth-авторизации через <a href="https://vk.com">ВКонтакте</a>.
 * <a href="https://vk.com/dev/access_token">Подробнее</a>.
 *
 * @author Anatoliy Pokhresnyi
 * @author dmitrymalinin
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
    private static final String DEFAULT_SCOPE = "";

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
        return getConfig().getUserInfoUrl();
    }

    @Override
    protected SimpleHttp buildUserInfoRequest(String subjectToken, String userInfoUrl) {
        return SimpleHttp.doGet(getConfig().getUserInfoUrl() + "&access_token=" + subjectToken, session);
    }

    @Override
    protected BrokeredIdentityContext extractIdentityFromProfile(EventBuilder event, JsonNode node) {
        logger.infof("ExtractIdentityFromProfile. Node %s", node);

        JsonNode context = Optional
            .ofNullable(StringUtils.asJsonNode(node, "response"))
            .map(e -> e.get(0))
            .orElse(node);

        logger.infof("ExtractIdentityFromProfile. Context %s", context);

        BrokeredIdentityContext user = new BrokeredIdentityContext(
            Objects.requireNonNull(StringUtils.asText(context, "id")),
            getConfig()
        );

        user.setUsername(StringUtils.asText(context, "screen_name"));
        user.setFirstName(StringUtils.asText(context, "first_name"));
        user.setLastName(StringUtils.asText(context, "last_name"));

        user.setIdp(this);

        AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, context, getConfig().getAlias());

        return user;
    }

    protected BrokeredIdentityContext extractIdentityFromProfile(JsonNode node, String email, String phone) {
        BrokeredIdentityContext user = extractIdentityFromProfile(null, node);

        if (getConfig().isEmailRequired() && StringUtils.isNullOrEmpty(email)) {
            throw new IllegalArgumentException(StringUtils.email("VK"));
        }

        if (StringUtils.nonNullOrEmpty(email)) {
            user.setUsername(email);
        } else {
            if (StringUtils.isNullOrEmpty(user.getUsername())) {
                user.setUsername("vk." + user.getId());
            }
        }

        user.setEmail(email);
        user.setUserAttribute("phone", phone);

        return user;
    }

    @Override
    public BrokeredIdentityContext getFederatedIdentity(String response) {
        logger.infof("GetFederatedIdentity %s", response);

        JsonNode node = StringUtils.asJsonNode(response);
        JsonNode context = StringUtils.asJsonNode(node, "response") == null
            ? node
            : StringUtils.asJsonNode(node, "response");
        String accessToken = StringUtils.asText(context, "access_token");
        String userId = StringUtils.asText(context, "user_id");
        String email = StringUtils.asText(context, "email");
        String phone = StringUtils.asText(context, "phone");

        if (accessToken == null) {
            throw new IdentityBrokerException("No access token available in OAuth server response: " + response);
        }

        BrokeredIdentityContext biContext = doGetFederatedIdentity(accessToken, userId, email, phone);
        biContext.getContextData().put(FEDERATED_ACCESS_TOKEN, accessToken);
        return biContext;
    }

    /**
     * Запрос информации о пользователе.
     *
     * @return Данные авторизованного пользователя.
     */
    protected BrokeredIdentityContext doGetFederatedIdentity(
        String accessToken, String userId, String email,
        String phone
    ) {
        try {
            String fields = StringUtils.isNullOrEmpty(getConfig().getFetchedFields())
                ? "" : "," + getConfig().getFetchedFields();
            String url = getConfig().getUserInfoUrl()
                + "&access_token=" + accessToken
                + "&user_ids=" + userId
                + "&fields=screen_name" + fields
                + "&name_case=Nom";

            return extractIdentityFromProfile(
                SimpleHttp
                    .doGet(url, session)
                    .param("content-type", "application/json; charset=utf-8")
                    .asJson(),
                email, phone
            );
        } catch (IOException e) {
            throw new IdentityBrokerException("Could not obtain user profile from VK: " + e.getMessage(), e);
        }
    }

    @Override
    protected BrokeredIdentityContext doGetFederatedIdentity(String accessToken) {
        try {
            String url = getConfig().getUserInfoUrl()
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
