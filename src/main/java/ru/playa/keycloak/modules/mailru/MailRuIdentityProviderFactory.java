package ru.playa.keycloak.modules.mailru;

import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;

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
    public MailRuIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new MailRuIdentityProvider(session, new MailRuIdentityProviderConfig(model));
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}