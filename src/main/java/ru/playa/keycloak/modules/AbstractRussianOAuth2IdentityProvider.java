package ru.playa.keycloak.modules;

import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

/**
 * Базовый провайдер OAuth-авторизации для российских социальных сетей.
 *
 * @param <C> Тип объекта настроек.
 * @author Anatoliy Pokhresnyi
 */
public abstract class AbstractRussianOAuth2IdentityProvider<C extends OAuth2IdentityProviderConfig>
    extends AbstractOAuth2IdentityProvider<C> {

    /**
     * Создает объект OAuth-авторизации для российских социальных сейтей.
     *
     * @param session Сессия Keycloak.
     * @param config  Конфигурация OAuth-авторизации.
     */
    public AbstractRussianOAuth2IdentityProvider(final KeycloakSession session, final C config) {
        super(session, config);

        logger.infof("Config %s", config.getConfig());
    }

    @Override
    public Object callback(final RealmModel realm, final AuthenticationCallback callback, final EventBuilder event) {
        return new AbstractRussianEndpoint(callback, event, this, session);
    }

}
