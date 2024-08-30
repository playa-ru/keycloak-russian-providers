package ru.playa.keycloak.modules.vkid;

import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import ru.playa.keycloak.modules.InfinispanUtils;

import java.util.List;

/**
 * Фабрика OAuth-авторизации через <a href="https://vk.com">ВКонтакте</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class VKIDIdentityProviderFactory
        extends AbstractIdentityProviderFactory<VKIDIdentityProvider>
        implements SocialIdentityProviderFactory<VKIDIdentityProvider> {

    /**
     * Уникальный идентификатор провайдера.
     */
    public static final String PROVIDER_ID = "vkid";


    @Override
    public String getName() {
        return "VK ID";
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        InfinispanUtils.init(factory.create());

        super.postInit(factory);
    }

    @Override
    public VKIDIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new VKIDIdentityProvider(session, new VKIDIdentityProviderConfig(model));
    }

    @Override
    public VKIDIdentityProviderConfig createConfig() {
        return new VKIDIdentityProviderConfig();
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder
            .create()
            .property()
            .name("emailRequired")
            .label("Email Required")
            .helpText("Is email required (user can be registered in VK via phone)")
            .type(ProviderConfigProperty.BOOLEAN_TYPE)
            .defaultValue("false")
            .add()
            .build();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}