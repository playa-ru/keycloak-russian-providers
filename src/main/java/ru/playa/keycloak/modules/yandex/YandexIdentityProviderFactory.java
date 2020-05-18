package ru.playa.keycloak.modules.yandex;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;

/**
 * Фабрика OAuth-авторизации через <a href="https://yandex.ru">Яндекс</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class YandexIdentityProviderFactory
        extends AbstractIdentityProviderFactory<YandexIdentityProvider>
        implements SocialIdentityProviderFactory<YandexIdentityProvider> {

    /**
     * Уникальный идентификатор провайдера.
     */
    public static final String PROVIDER_ID = "yandex";

    @Override
    public String getName() {
        return "Yandex";
    }

    @Override
    public YandexIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new YandexIdentityProvider(session, new YandexIdentityProviderConfig(model));
    }

    @Override
    public YandexIdentityProviderConfig createConfig() {
        return new YandexIdentityProviderConfig();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}