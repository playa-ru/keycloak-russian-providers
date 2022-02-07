package ru.playa.keycloak.modules.mailru;

import org.keycloak.broker.oidc.OIDCIdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;

/**
 * Настройки OAuth-авторизации через <a href="https://my.mail.ru">Мой Мир</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class MailRuIdentityProviderConfig
        extends OIDCIdentityProviderConfig {

    /**
     * Создает объект настроек OAuth-авторизации через
     * <a href="https://my.mail.ru">Мой Мир</a>.
     *
     * @param model Модель настроек OAuth-авторизации.
     */
    public MailRuIdentityProviderConfig(IdentityProviderModel model) {
        super(model);
    }

    /**
     * Создает объект настроек OAuth-авторизации через
     * <a href="https://my.mail.ru">Мой Мир</a>.
     */
    public MailRuIdentityProviderConfig() {
    }
}