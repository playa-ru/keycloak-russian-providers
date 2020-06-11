package ru.playa.keycloak.modules.ok;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;

/**
 * Настройки OAuth-авторизации через <a href="https://ok.ru/">Одноклассники</a>.
 *
 * @author Anatoliy Pokhresnyi
 * @author dmitrymalinin
 */
public class OKIdentityProviderConfig
        extends OAuth2IdentityProviderConfig {

    /**
     * Создает объект настроек OAuth-авторизации через
     * <a href="https://ok.ru/">Одноклассники</a>.
     *
     * @param model Модель настроек OAuth-авторизации.
     */
    public OKIdentityProviderConfig(IdentityProviderModel model) {
        super(model);
    }

    /**
     * Создает объект настроек OAuth-авторизации через
     * <a href="https://ok.ru/">Одноклассники</a>.
     */
    public OKIdentityProviderConfig() {
    }

    /**
     * Получения публичного ключа приложения.
     *
     * @return Публичный ключ приложения.
     */
    public String getPublicKey() {
        return getConfig().get("public_key");
    }
    
    /**
     * Требуется email в профиле пользователя.
     *
     * @return Требуется email в профиле пользователя.
     */
    public boolean isEmailRequired() {
        return Boolean.parseBoolean(getConfig().getOrDefault("email_required", "false"));
    }
}