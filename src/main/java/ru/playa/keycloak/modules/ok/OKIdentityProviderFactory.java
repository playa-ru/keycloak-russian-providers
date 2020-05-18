package ru.playa.keycloak.modules.ok;

import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import ru.playa.keycloak.modules.vk.VKIdentityProviderConfig;

/**
 * Фабрика OAuth-авторизации через <a href="https://my.mail.ru">Мой Мир</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class OKIdentityProviderFactory
        extends AbstractIdentityProviderFactory<OKIdentityProvider>
        implements SocialIdentityProviderFactory<OKIdentityProvider> {

    /**
     * Уникальный идентификатор провайдера.
     */
    public static final String PROVIDER_ID = "ok";

    @Override
    public String getName() {
        return "OK";
    }

    @Override
    public OKIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new OKIdentityProvider(session, new OKIdentityProviderConfig(model));
    }

    @Override
    public OKIdentityProviderConfig createConfig() {
        return new OKIdentityProviderConfig();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}