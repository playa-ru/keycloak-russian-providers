package ru.playa.keycloak.modules.vk;

import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.List;

/**
 * Фабрика OAuth-авторизации через <a href="https://vk.com">ВКонтакте</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class VKIdentityProviderFactory
        extends AbstractIdentityProviderFactory<VKIdentityProvider>
        implements SocialIdentityProviderFactory<VKIdentityProvider> {

    /**
     * Уникальный идентификатор провайдера.
     */
    public static final String PROVIDER_ID = "vk";

    @Override
    public String getName() {
        return "VK";
    }

    @Override
    public VKIdentityProvider create(final KeycloakSession session, final IdentityProviderModel model) {
        return new VKIdentityProvider(session, new VKIdentityProviderConfig(model));
    }

    @Override
    public VKIdentityProviderConfig createConfig() {
        return new VKIdentityProviderConfig();
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder
            .create()
            .property()
            .name("version")
            .label("Version VK API")
            .helpText("Version of VK API.")
            .type(ProviderConfigProperty.STRING_TYPE)
            .add()
            .property()
            .name("emailRequired")
            .label("Email Required")
            .helpText("Is email required (user can be registered in VK via phone)")
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