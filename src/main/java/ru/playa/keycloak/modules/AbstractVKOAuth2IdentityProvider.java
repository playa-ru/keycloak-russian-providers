package ru.playa.keycloak.modules;

import com.fasterxml.jackson.databind.JsonNode;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Базовый провайдер OAuth-авторизации через <a href="https://vk.com">ВКонтакте</a>.
 *
 * @param <T> Тип настроек OAuth-авторизации
 * @author Anatoliy Pokhresnyi
 */
public abstract class AbstractVKOAuth2IdentityProvider<T extends AbstractVKIdentityProviderConfig>
extends AbstractRussianOAuth2IdentityProvider<T>
implements SocialIdentityProvider<T> {

    /**
     * Права доступа к данным пользователя по умолчанию.
     */
    private static final String DEFAULT_SCOPE = "";

    /**
     * Список дополнительных настроек провайдера авторизации.
     */
    public static final List<ProviderConfigProperty> CONFIG_PROPERTIES = ProviderConfigurationBuilder
            .create()
            .property()
            .name("version")
            .label("Version VK API")
            .helpText("Version of VK API.")
            .type(ProviderConfigProperty.STRING_TYPE)
            .add()
            .property()
            .name("emailRequired")
            .label("Email Required")
            .helpText("Is email required (user can be registered in VK via phone)")
            .type(ProviderConfigProperty.BOOLEAN_TYPE)
            .defaultValue("false")
            .add()
            .property()
            .name("fetchedFields")
            .label("Fetched Fields")
            .helpText("Additional fields to need to be fetched")
            .type(ProviderConfigProperty.STRING_TYPE)
            .add()
            .build();


    /**
     * Создает объект OAuth-авторизации для российских социальных сейтей.
     *
     * @param session Сессия Keycloak.
     * @param config  Конфигурация OAuth-авторизации.
     */
    public AbstractVKOAuth2IdentityProvider(KeycloakSession session, T config) {
        super(session, config);
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

    public KeycloakSession getSession() {
        return session;
    }

    @Override
    protected BrokeredIdentityContext extractIdentityFromProfile(EventBuilder event, JsonNode node) {
        logger.infof("ExtractIdentityFromProfile. Node %s", node);

        JsonNode context = JsonUtils.asJsonNode(node, "response").get(0);

        logger.infof("ExtractIdentityFromProfile. Context %s", context);

        BrokeredIdentityContext user = new BrokeredIdentityContext(
            Objects.requireNonNull(JsonUtils.asText(context, "id")),
            getConfig()
        );

        user.setUsername(JsonUtils.asText(context, "screen_name"));
        user.setFirstName(JsonUtils.asText(context, "first_name"));
        user.setLastName(JsonUtils.asText(context, "last_name"));

        user.setIdp(this);

        AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, context, getConfig().getAlias());

        return user;
    }

    protected BrokeredIdentityContext extractIdentityFromProfile(JsonNode node, String email, String phone) {
        BrokeredIdentityContext user = extractIdentityFromProfile(null, node);

        if (getConfig().isEmailRequired() && StringUtils.isNullOrEmpty(email)) {
            throw new IllegalArgumentException(MessageUtils.email("VK"));
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

        JsonNode node = JsonUtils.asJsonNode(response);
        JsonNode context = JsonUtils.asJsonNode(node, "response") == null
            ? node
            : JsonUtils.asJsonNode(node, "response");
        String accessToken = JsonUtils.asText(context, "access_token");
        String userId = JsonUtils.asText(context, "user_id");
        String email = JsonUtils.asText(context, "email");
        String phone = JsonUtils.asText(context, "phone");

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
    protected BrokeredIdentityContext doGetFederatedIdentity(String accessToken, String userId, String email,
                                                             String phone) {
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
                    email, phone);
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
