package ru.playa.keycloak.modules.mailru;

import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.Urls;
import ru.playa.keycloak.modules.AbstractRussianEndpoint;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE;
import static org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_CODE;
import static org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_GRANT_TYPE;
import static org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_REDIRECT_URI;

/**
 * Переопределенный класс {@code AbstractRussianEndpoint}.
 * Класс переопределен с целью изменения логики замены кода на токен.
 */
public class MailRuEndpoint extends AbstractRussianEndpoint {

    /**
     * Провайдер авторизации.
     */
    private final MailRuIdentityProvider provider;

    /**
     * Сессия.
     */
    private final KeycloakSession session;

    /**
     * Контекст.
     */
    private final KeycloakContext context;

    /**
     * Конструктор.
     *
     * @param aCallback Callback.
     * @param aEvent    Сервис отправки событий.
     * @param aProvider Провайдер авторизации.
     * @param aSession  Сессия.
     */
    public MailRuEndpoint(
        final IdentityProvider.AuthenticationCallback aCallback,
        final EventBuilder aEvent,
        final MailRuIdentityProvider aProvider,
        final KeycloakSession aSession
    ) {
        super(aCallback, aEvent, aProvider, aSession);
        this.provider = aProvider;
        this.session = aSession;
        this.context = aSession.getContext();
    }

    @Override
    public SimpleHttp generateTokenRequest(final String authorizationCode) {
        String clientID = provider.getConfig().getClientId();
        String clientSecret = provider.getConfig().getClientSecret();
        String credentials = Base64
            .getEncoder()
            .encodeToString((clientID + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

        return SimpleHttp
            .doPost(provider.getConfig().getTokenUrl(), session)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("Authorization", "Basic " + credentials)
            .param(OAUTH2_PARAMETER_CODE, authorizationCode)
            .param(OAUTH2_PARAMETER_REDIRECT_URI, Urls
                .identityProviderAuthnResponse(
                    context.getUri().getBaseUri(), provider.getConfig().getAlias(), context.getRealm().getName()
                )
                .toString())
            .param(OAUTH2_PARAMETER_GRANT_TYPE, OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE);
    }
}