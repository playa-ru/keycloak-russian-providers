package ru.playa.keycloak.modules.vkid;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.events.EventBuilder;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.Urls;
import ru.playa.keycloak.modules.AbstractRussianEndpoint;
import ru.playa.keycloak.modules.InfinispanUtils;

import static org.keycloak.OAuth2Constants.ERROR;
import static org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE;
import static org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_CLIENT_ID;
import static org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_CODE;
import static org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_GRANT_TYPE;
import static org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_REDIRECT_URI;
import static org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_STATE;

/**
 * Переопределенный класс {@code AbstractRussianEndpoint}.
 * Класс переопределен с целью изменения логики замены кода на токен.
 */
public class VKIDEndpoint extends AbstractRussianEndpoint {

    /**
     * Провайдер авторизации.
     */
    private final VKIDIdentityProvider provider;

    /**
     * Сессия.
     */
    private final KeycloakSession session;

    /**
     * Контекст.
     */
    private final KeycloakContext context;

    /**
     * HTTP запрос.
     */
    private final HttpRequest request;

    /**
     * Конструктор.
     *
     * @param aCallback Callback.
     * @param aEvent    Сервис отправки событий.
     * @param aProvider Провайдер авторизации.
     * @param aSession  Сессия.
     */
    public VKIDEndpoint(
        final IdentityProvider.AuthenticationCallback aCallback,
        final EventBuilder aEvent,
        final VKIDIdentityProvider aProvider,
        final KeycloakSession aSession
    ) {
        super(aCallback, aEvent, aProvider, aSession);
        this.provider = aProvider;
        this.session = aSession;
        this.context = aSession.getContext();
        this.request = aSession.getContext().getHttpRequest();
    }

    @GET
    @Path("")
    @Override
    public Response authResponse(
        @QueryParam(OAUTH2_PARAMETER_STATE) final String state,
        @QueryParam(OAUTH2_PARAMETER_CODE) final String authorizationCode,
        @QueryParam(ERROR) final String error
    ) {
        String oldState = InfinispanUtils.get(state);

        return super.authResponse(oldState, authorizationCode, error);
    }

    @Override
    public SimpleHttp generateTokenRequest(final String authorizationCode) {
        String deviceID = request.getUri().getQueryParameters().getFirst("device_id");
        String state = request.getUri().getQueryParameters().getFirst("state");
        String oldState = InfinispanUtils.get(state);
        String codeVerifier = InfinispanUtils.get(oldState);

        return SimpleHttp
            .doPost(provider.getConfig().getTokenUrl(), session)
            .param(OAUTH2_PARAMETER_CODE, authorizationCode)
            .param(
                OAUTH2_PARAMETER_REDIRECT_URI,
                Urls
                    .identityProviderAuthnResponse(
                        context.getUri().getBaseUri(), provider.getConfig().getAlias(), context.getRealm().getName()
                    )
                    .toString()
            )
            .param(OAUTH2_PARAMETER_GRANT_TYPE, OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE)
            .param(OAUTH2_PARAMETER_CLIENT_ID, provider.getConfig().getClientId())
            .param("device_id", deviceID)
            .param("code_verifier", codeVerifier)
            .param("state", state);
    }
}
