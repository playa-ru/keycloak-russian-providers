package ru.playa.keycloak.modules.mailru;

import org.keycloak.broker.oidc.OIDCIdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;
import ru.playa.keycloak.modules.Utils;

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
    public MailRuIdentityProviderConfig(final IdentityProviderModel model) {
        super(model);
    }

    /**
     * Создает объект настроек OAuth-авторизации через
     * <a href="https://my.mail.ru">Мой Мир</a>.
     */
    public MailRuIdentityProviderConfig() {
    }

    /**
     * Получения белого списка доменов.
     *
     * @return Белый список доменов.
     */
    public String getHostedDomain() {
        String mailHostedDomain = this.getConfig().get("mailHostedDomain");

        return Utils.nonNullOrEmpty(mailHostedDomain) ? mailHostedDomain : null;
    }
}