package ru.playa.keycloak.modules;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;

/**
 * Настройки OAuth-авторизации через <a href="https://vk.com">ВКонтакте</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public abstract class AbstractVKIdentityProviderConfig extends OAuth2IdentityProviderConfig {
    /**
     * Получает версию API ВКонтакте.
     *
     * @return Версию API ВКонтакте.
     */
    public abstract String getVersion();

    /**
     * Требуется email в профиле пользователя.
     *
     * @return Требуется email в профиле пользователя.
     */
    public abstract boolean isEmailRequired();

    /**
     * Дополнительные поля из профиля пользователя.
     *
     * @return Дополнительные поля из профиля пользователя
     */
    public abstract String getFetchedFields();

    /**
     * Создает объект настроек OAuth-авторизации через
     * <a href="https://vk.com">ВКонтакте</a>.
     */
    public AbstractVKIdentityProviderConfig() {
    }

    /**
     * Создает объект настроек OAuth-авторизации через
     * <a href="https://vk.com">ВКонтакте</a>.
     *
     * @param model Модель настроек OAuth-авторизации.
     */
    public AbstractVKIdentityProviderConfig(IdentityProviderModel model) {
        super(model);
    }
}