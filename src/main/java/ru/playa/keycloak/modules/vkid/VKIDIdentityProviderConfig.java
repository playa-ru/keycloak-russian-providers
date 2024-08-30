package ru.playa.keycloak.modules.vkid;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;

/**
 * Настройки OAuth-авторизации через <a href="https://vk.com">ВКонтакте</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class VKIDIdentityProviderConfig
    extends OAuth2IdentityProviderConfig {

    /**
     * Создает объект настроек OAuth-авторизации через
     * <a href="https://vk.com">ВКонтакте</a>.
     *
     * @param model Модель настроек OAuth-авторизации.
     */
    public VKIDIdentityProviderConfig(final IdentityProviderModel model) {
        super(model);
    }

    /**
     * Создает объект настроек OAuth-авторизации через
     * <a href="https://vk.com">ВКонтакте</a>.
     */
    public VKIDIdentityProviderConfig() {
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
