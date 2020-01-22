package ru.playa.keycloak.modules.ok;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;

/**
 * Настройки OAuth-авторизации через <a href="https://ok.ru/">Одноклассники</a>.
 *
 * @author Anatoliy Pokhresnyi
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
     * Получения публичного ключа приложения.
     *
     * @return Публичный ключ приложения.
     */
    public String getPublicKey() {
        return getConfig().get("public_key");
    }
}
