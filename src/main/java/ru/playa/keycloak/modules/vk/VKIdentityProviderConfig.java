package ru.playa.keycloak.modules.vk;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;

/**
 * Настройки OAuth-авторизации через <a href="https://vk.com">ВКонтакте</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class VKIdentityProviderConfig
        extends OAuth2IdentityProviderConfig {

    /**
     * Создает объект настроек OAuth-авторизации через
     * <a href="https://vk.com">ВКонтакте</a>.
     *
     * @param model Модель настроек OAuth-авторизации.
     */
    public VKIdentityProviderConfig(IdentityProviderModel model) {
        super(model);
    }

    /**
     * Получает версию API ВКонтакте.
     *
     * @return Версию API ВКонтакте.
     */
    public String getVersion() {
        return getConfig().get("version");
    }
}
