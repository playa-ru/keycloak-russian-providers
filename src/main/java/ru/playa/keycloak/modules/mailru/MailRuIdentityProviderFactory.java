package ru.playa.keycloak.modules.mailru;

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
public class MailRuIdentityProviderFactory
        extends AbstractIdentityProviderFactory<MailRuIdentityProvider>
        implements SocialIdentityProviderFactory<MailRuIdentityProvider> {

    /**
     * Уникальный идентификатор провайдера.
     */
    public static final String PROVIDER_ID = "mailru";

    @Override
    public String getName() {
        return "MailRu";
    }

    @Override
    public MailRuIdentityProvider create(final KeycloakSession session, final IdentityProviderModel model) {
        return new MailRuIdentityProvider(session, new MailRuIdentityProviderConfig(model));
    }

    @Override
    public MailRuIdentityProviderConfig createConfig() {
        return new MailRuIdentityProviderConfig();
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name("mailHostedDomain")
                .label("Hosted domains")
                .helpText("Comma ',' separated list of domains is supported.")
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .build();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}