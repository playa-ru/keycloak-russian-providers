package ru.playa.keycloak.modules.mailru;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;

/**
 * Настройки OAuth-авторизации через <a href="https://my.mail.ru">Мой Мир</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class MailRuIdentityProviderConfig
        extends OAuth2IdentityProviderConfig {

    /**
     * Создает объект настроек OAuth-авторизации через
     * <a href="https://my.mail.ru">Мой Мир</a>.
     *
     * @param model Модель настроек OAuth-авторизации.
     */
    public MailRuIdentityProviderConfig(IdentityProviderModel model) {
        super(model);
    }
}
