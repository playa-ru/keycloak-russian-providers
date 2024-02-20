package ru.playa.keycloak.modules.vkid;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import ru.playa.keycloak.modules.AbstractVKOAuth2IdentityProvider;
import ru.playa.keycloak.modules.JsonUtils;

import java.util.HashMap;
import java.util.Map;
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
    private static final String AUTH_URL = "https://id.vk.com/auth";

    /**
     * Обмен кода подтверждения на токен.
     */
    private static final String TOKEN_URL = "https://api.vk.com/method/auth.exchangeSilentAuthToken";

    /**
     * Запрос информации о пользователе.
     */
    private static final String PROFILE_URL = "https://api.vk.com/method/users.get";

    /**
     * Кеш state-ов.
     */
    private static final Map<String, String> CACHE = new HashMap<>();

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
        config.setTokenUrl(TOKEN_URL + "?v=" + getConfig().getVersion());
        config.setUserInfoUrl(PROFILE_URL + "?v=" + getConfig().getVersion());
    }

    @Override
    public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
        return new VkEndpoint(callback, realm, event, this);
    }

    @Override
    protected UriBuilder createAuthorizationUrl(AuthenticationRequest request) {
        final String state = UUID.randomUUID().toString();

        CACHE.put(state, request.getState().getEncoded());

        return UriBuilder
                .fromUri(getConfig().getAuthorizationUrl())
                .queryParam(OAUTH2_PARAMETER_SCOPE, getConfig().getDefaultScope())
                .queryParam("uuid", state)
                .queryParam(OAUTH2_PARAMETER_RESPONSE_TYPE, "silent_token")
                .queryParam("app_id", getConfig().getClientId())
                .queryParam(OAUTH2_PARAMETER_REDIRECT_URI, request.getRedirectUri());
    }

    /**
     * Переопределенный класс {@link AbstractOAuth2IdentityProvider.Endpoint}.
     * Класс переопределен с целью возвращения человеко-читаемой ошибки если
     * в профиле социальной сети не указана электронная почта.
     */
    protected static class VkEndpoint extends AbstractRussianEndpoint<VKIDIdentityProvider> {

        public VkEndpoint(
            AuthenticationCallback callback,
            RealmModel realm,
            EventBuilder event,
            VKIDIdentityProvider provider
        ) {
            super(callback, realm, event, provider);
        }

        @GET
        public Response authResponse(
                @QueryParam(AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_STATE) String state,
                @QueryParam(AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_CODE) String authorizationCode,
                @QueryParam(OAuth2Constants.ERROR) String error
        ) {
            String payload = httpRequest.getUri().getQueryParameters().getFirst("payload");

            logger.infof("VkEndpoint. AuthResponse. Payload %s", payload);

            JsonNode node = JsonUtils.asJsonNode(payload);

            logger.infof("VkEndpoint. AuthResponse. Node %s", node);

            String token = JsonUtils.asText(node, "token");
            String uuid = JsonUtils.asText(node, "uuid");
            String oldState = CACHE.getOrDefault(uuid, uuid);

            return super.authResponse(oldState, token, error);
        }

        public SimpleHttp generateTokenRequest(String authorizationCode) {
            String payload = httpRequest.getUri().getQueryParameters().getFirst("payload");
            JsonNode node = JsonUtils.asJsonNode(payload);
            String uuid = JsonUtils.asText(node, "uuid");

            return SimpleHttp
                    .doPost(getProvider().getConfig().getTokenUrl(), session)
                    .param("v", getProvider().getConfig().getVersion())
                    .param("token", authorizationCode)
                    .param("access_token", getProvider().getConfig().getClientSecret())
                    .param("uuid", uuid);

        }
    }
}
