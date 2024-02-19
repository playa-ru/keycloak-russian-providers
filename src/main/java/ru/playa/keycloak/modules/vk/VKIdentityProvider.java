package ru.playa.keycloak.modules.vk;

import org.keycloak.models.KeycloakSession;
import ru.playa.keycloak.modules.AbstractVKOAuth2IdentityProvider;

/**
 * Провайдер OAuth-авторизации через <a href="https://vk.com">ВКонтакте</a>.
 * <a href="https://vk.com/dev/access_token">Подробнее</a>.
 *
 * @author Anatoliy Pokhresnyi
 * @author dmitrymalinin
 */
public class VKIdentityProvider
        extends AbstractVKOAuth2IdentityProvider<VKIdentityProviderConfig> {

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

}
