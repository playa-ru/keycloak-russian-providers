package ru.playa.keycloak.modules.vkid;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.core.UriBuilder;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import ru.playa.keycloak.modules.*;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Провайдер OAuth-авторизации через <a href="https://vk.com">ВКонтакте</a>.
 * <a href="https://id.vk.com/about/business">Подробнее</a>.
 *
 * @author Anatoliy Pokhresnyi
 * @author dmitrymalinin
 */
public class VKIDIdentityProvider
        extends AbstractVKOAuth2IdentityProvider<VKIDIdentityProviderConfig> {

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
    public VKIDIdentityProvider(KeycloakSession session, VKIDIdentityProviderConfig config) {
        super(session, config);

        config.setAuthorizationUrl(AUTH_URL);
        config.setTokenUrl(TOKEN_URL);
        config.setUserInfoUrl(PROFILE_URL);
    }

    @Override
    public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
        return new VkEndpoint(callback, realm, event, this, this.getSession());
    }

    /**
     * Запрос информации о пользователе.
     *
     * @return Данные авторизованного пользователя.
     */
    protected BrokeredIdentityContext doGetFederatedIdentity(String accessToken, String userId, String email,
                                                             String phone) {
        try {
            logger.infof("DoGetFederatedIdentity AccessToken %s", accessToken);

            return extractIdentityFromProfile(
                    SimpleHttp
                            .doPost(getConfig().getUserInfoUrl(), session)
                            .param("access_token", accessToken)
                            .param("client_id", getConfig().getClientId())
                            .asJson(),
                    email, phone);
        } catch (IOException e) {
            throw new IdentityBrokerException("Could not obtain user profile from VK: " + e.getMessage(), e);
        }
    }

    @Override
    protected BrokeredIdentityContext extractIdentityFromProfile(EventBuilder event, JsonNode node) {
        logger.infof("ExtractIdentityFromProfile. Node %s", node);

        JsonNode context = JsonUtils.asJsonNode(node, "user");

        logger.infof("ExtractIdentityFromProfile. Context %s", context);

        BrokeredIdentityContext user = new BrokeredIdentityContext(
                Objects.requireNonNull(JsonUtils.asText(context, "user_id")),
                getConfig()
        );

        user.setFirstName(JsonUtils.asText(context, "first_name"));
        user.setLastName(JsonUtils.asText(context, "last_name"));

        user.setIdp(this);

        AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, context, getConfig().getAlias());

        return user;
    }

    @Override
    protected UriBuilder createAuthorizationUrl(AuthenticationRequest request) {
        final String state = UUID.randomUUID().toString();
        final String code = new PkceGeneratorSHA256().generateRandomCodeVerifier(new SecureRandom());

        InfinispanUtils.put(state, request.getState().getEncoded());
        InfinispanUtils.put(request.getState().getEncoded(), code);

        UriBuilder b = UriBuilder
                .fromUri(getConfig().getAuthorizationUrl())
                .queryParam(OAUTH2_PARAMETER_SCOPE, getConfig().getDefaultScope())
                .queryParam("state", state)
                .queryParam("code_challenge_method", "s256")
                .queryParam("code_challenge", new PkceGeneratorSHA256().deriveCodeVerifierChallenge(code))
                .queryParam(OAUTH2_PARAMETER_RESPONSE_TYPE, "code")
                .queryParam("client_id", getConfig().getClientId())
                .queryParam(OAUTH2_PARAMETER_REDIRECT_URI, request.getRedirectUri());

        logger.infof("VKIDIdentityProvider CreateAuthorizationUrl code_challenge %s", MD5Utils.sha256(code));
        logger.infof("VKIDIdentityProvider CreateAuthorizationUrl code_verifier %s", code);

        return b;
    }

}
