package ru.playa.keycloak.modules.vkid;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.core.UriBuilder;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import ru.playa.keycloak.modules.AbstractRussianOAuth2IdentityProvider;
import ru.playa.keycloak.modules.InfinispanUtils;
import ru.playa.keycloak.modules.Utils;

import java.io.IOException;
import java.util.UUID;

/**
 * Провайдер OAuth-авторизации через <a href="https://vk.com">ВКонтакте</a>.
 * <a href="https://id.vk.com/about/business">Подробнее</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class VKIDIdentityProvider
    extends AbstractRussianOAuth2IdentityProvider<VKIDIdentityProviderConfig>
    implements SocialIdentityProvider<VKIDIdentityProviderConfig> {

    /**
     * Запрос кода подтверждения.
     */
    private static final String AUTH_URL = "https://id.vk.com/authorize";

    /**
     * Обмен кода подтверждения на токен.
     */
    private static final String TOKEN_URL = "https://id.vk.com/oauth2/auth";

    /**
     * Запрос информации о пользователе.
     */
    private static final String PROFILE_URL = "https://id.vk.com/oauth2/user_info";

    /**
     * Создает объект OAuth-авторизации через
     * <a href="https://vk.com">ВКонтакте</a>.
     *
     * @param session Сессия Keycloak.
     * @param config  Конфигурация OAuth-авторизации.
     */
    public VKIDIdentityProvider(final KeycloakSession session, final VKIDIdentityProviderConfig config) {
        super(session, config);

        config.setAuthorizationUrl(AUTH_URL);
        config.setTokenUrl(TOKEN_URL);
        config.setUserInfoUrl(PROFILE_URL);
    }

    @Override
    public Object callback(final RealmModel realm, final AuthenticationCallback callback, final EventBuilder event) {
        return new VKIDEndpoint(callback, event, this, session);
    }

    @Override
    public BrokeredIdentityContext getFederatedIdentity(final String response) {
        logger.infof("GetFederatedIdentity %s", response);

        JsonNode node = Utils.asJsonNode(response);
        String accessToken = Utils.asText(node, "access_token");

        if (accessToken == null) {
            throw new IdentityBrokerException("No access token available in OAuth server response: " + response);
        }

        BrokeredIdentityContext biContext = doGetFederatedIdentity(accessToken);
        biContext.getContextData().put(FEDERATED_ACCESS_TOKEN, accessToken);
        return biContext;
    }

    /**
     * Запрос информации о пользователе.
     *
     * @return Данные авторизованного пользователя.
     */
    @Override
    protected BrokeredIdentityContext doGetFederatedIdentity(final String accessToken) {
        try {
            logger.infof("DoGetFederatedIdentity AccessToken %s", accessToken);

            return extractIdentityFromProfile(
                    SimpleHttp
                            .doPost(getConfig().getUserInfoUrl(), session)
                            .param("access_token", accessToken)
                            .param("client_id", getConfig().getClientId())
                            .asJson()
            );
        } catch (IOException e) {
            throw new IdentityBrokerException("Could not obtain user profile from VK: " + e.getMessage(), e);
        }
    }

    /**
     * Получение BrokeredIdentityContext из профиля пользователя.
     *
     * @param node Профиль пользователя.
     * @return BrokeredIdentityContext
     */
    protected BrokeredIdentityContext extractIdentityFromProfile(final JsonNode node) {
        BrokeredIdentityContext user = extractIdentityFromProfile(null, node);

        String email = Utils.asText(node, "email");
        String phone = Utils.asText(node, "phone");

        if (getConfig().isEmailRequired() && Utils.isNullOrEmpty(email)) {
            throw new IllegalArgumentException(Utils.toEmailErrorMessage("VK"));
        }

        if (Utils.nonNullOrEmpty(email)) {
            user.setUsername(email);
        } else {
            if (Utils.isNullOrEmpty(user.getUsername())) {
                user.setUsername("vk." + user.getId());
            }
        }

        user.setEmail(email);
        user.setUserAttribute("phone", phone);

        return user;
    }


    @Override
    protected BrokeredIdentityContext extractIdentityFromProfile(final EventBuilder event, final JsonNode node) {
        JsonNode context = Utils.asJsonNode(node, "user");
        BrokeredIdentityContext user = new BrokeredIdentityContext(Utils.asText(context, "user_id"));

        user.setFirstName(Utils.asText(context, "first_name"));
        user.setLastName(Utils.asText(context, "last_name"));

        user.setIdp(this);
        user.setIdpConfig(getConfig());

        AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, context, getConfig().getAlias());

        return user;
    }

    @Override
    protected UriBuilder createAuthorizationUrl(final AuthenticationRequest request) {
        final String state = UUID.randomUUID().toString();
        final String code = Utils.getRandomString();

        InfinispanUtils.put(state, request.getState().getEncoded());
        InfinispanUtils.put(request.getState().getEncoded(), code);

        return UriBuilder
                .fromUri(getConfig().getAuthorizationUrl())
                .queryParam(OAUTH2_PARAMETER_SCOPE, getConfig().getDefaultScope())
                .queryParam("state", state)
                .queryParam("code_challenge_method", "s256")
                .queryParam("code_challenge", Utils.sha256(code))
                .queryParam(OAUTH2_PARAMETER_RESPONSE_TYPE, "code")
                .queryParam("client_id", getConfig().getClientId())
                .queryParam(OAUTH2_PARAMETER_REDIRECT_URI, request.getRedirectUri());
    }

    @Override
    protected String getDefaultScopes() {
        return "";
    }
}
