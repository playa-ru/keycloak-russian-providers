package ru.playa.keycloak.modules.vkid;

import jakarta.ws.rs.core.UriBuilder;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import ru.playa.keycloak.modules.AbstractVKOAuth2IdentityProvider;
import ru.playa.keycloak.modules.InfinispanUtils;
import ru.playa.keycloak.modules.MD5Utils;
import ru.playa.keycloak.modules.PasswordUtils;

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
    private static final String PROFILE_URL = "https://api.vk.com/method/users.get";

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

    @Override
    protected UriBuilder createAuthorizationUrl(AuthenticationRequest request) {
        final String state = UUID.randomUUID().toString();
        final String code = PasswordUtils.get();

        InfinispanUtils.put(state, request.getState().getEncoded());
        InfinispanUtils.put(request.getState().getEncoded(), code);

        UriBuilder b = UriBuilder
                .fromUri(getConfig().getAuthorizationUrl())
                .queryParam(OAUTH2_PARAMETER_SCOPE, getConfig().getDefaultScope())
                .queryParam("state", state)
                .queryParam("code_challenge_method", "s256")
                .queryParam("code_challenge", MD5Utils.sha256(code))
                .queryParam(OAUTH2_PARAMETER_RESPONSE_TYPE, "code")
                .queryParam("client_id", getConfig().getClientId())
                .queryParam(OAUTH2_PARAMETER_REDIRECT_URI, request.getRedirectUri());

        logger.infof("VKIDIdentityProvider CreateAuthorizationUrl code_challenge %s", MD5Utils.sha256(code));
        logger.infof("VKIDIdentityProvider CreateAuthorizationUrl code_verifier %s", code);

        return b;
    }

}
