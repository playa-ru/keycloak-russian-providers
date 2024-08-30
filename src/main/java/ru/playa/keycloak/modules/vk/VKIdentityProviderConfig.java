package ru.playa.keycloak.modules.vk;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;

/**
 * Настройки OAuth-авторизации через <a href="https://vk.com">ВКонтакте</a>.
 *
 * @author Anatoliy Pokhresnyi
 * @author dmitrymalinin
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
     * Создает объект настроек OAuth-авторизации через
     * <a href="https://vk.com">ВКонтакте</a>.
     */
    public VKIdentityProviderConfig() {
    }

    /**
     * Получает версию API ВКонтакте.
     *
     * @return Версию API ВКонтакте.
     */
    public String getVersion() {
        return getConfig().get("version");
    }

    /**
     * Требуется email в профиле пользователя.
     *
     * @return Требуется email в профиле пользователя.
     */
    public boolean isEmailRequired() {
        return Boolean.parseBoolean(getConfig().getOrDefault("emailRequired", "false"));
    }

    /**
     * Дополнительные поля из профиля пользователя.
     *
     * @return Дополнительные поля из профиля пользователя
     */
    public String getFetchedFields() {
        return getConfig().get("fetchedFields");
    }

}
