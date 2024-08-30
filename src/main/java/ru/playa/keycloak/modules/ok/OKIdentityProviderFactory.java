package ru.playa.keycloak.modules.ok;

import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.List;

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
    public OKIdentityProvider create(final KeycloakSession session, final IdentityProviderModel model) {
        return new OKIdentityProvider(session, new OKIdentityProviderConfig(model));
    }

    @Override
    public OKIdentityProviderConfig createConfig() {
        return new OKIdentityProviderConfig();
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder
            .create()
            .property()
            .name("publicKey")
            .label("Public Key")
            .helpText("Public Key")
            .type(ProviderConfigProperty.STRING_TYPE)
            .add()
            .property()
            .name("emailRequired")
            .label("Email Required")
            .helpText("Is email required (user can be registered in OK via phone)")
            .type(ProviderConfigProperty.BOOLEAN_TYPE)
            .defaultValue("false")
            .add()
            .property()
            .name("fetchedFields")
            .label("Fetched Fields")
            .helpText("Additional fields to need to be fetched")
            .type(ProviderConfigProperty.STRING_TYPE)
            .add()
            .build();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}