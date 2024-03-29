package ru.playa.keycloak.modules.yandex;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;
import ru.playa.keycloak.modules.StringUtils;

/**
 * Настройки OAuth-авторизации через <a href="https://yandex.ru">Яндекс</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class YandexIdentityProviderConfig
        extends OAuth2IdentityProviderConfig {

    /**
     * Создает объект настроек OAuth-авторизации через
     * <a href="https://yandex.ru">Яндекс</a>.
     *
     * @param model Модель настроек OAuth-авторизации.
     */
    public YandexIdentityProviderConfig(IdentityProviderModel model) {
        super(model);
    }

    /**
     * Создает объект настроек OAuth-авторизации через
     * <a href="https://yandex.ru">Яндекс</a>.
     */
    public YandexIdentityProviderConfig() {
    }

    /**
     * Получения белого списка доменов.
     *
     * @return Белый список доменов.
     */
    public String getHostedDomain() {
        String hostedDomain = this.getConfig().get("hostedDomain");

        return StringUtils.nonNullOrEmpty(hostedDomain) ? hostedDomain : null;
    }
}