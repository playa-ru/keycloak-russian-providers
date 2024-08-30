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
import static org.keycloak.OAuth2Constants.ERROR_DESCRIPTION;
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

    private final VKIDIdentityProvider provider;
    private final KeycloakContext context;
    private final HttpRequest request;
    private final KeycloakSession session;

    public VKIDEndpoint(
        IdentityProvider.AuthenticationCallback aCallback,
        EventBuilder aEvent,
        VKIDIdentityProvider aProvider,
        KeycloakSession aSession
    ) {
        super(aCallback, aEvent, aProvider, aSession);
        this.provider = aProvider;
        this.session = aSession;
        this.context = aSession.getContext();
        this.request = aSession.getContext().getHttpRequest();
    }

    @GET
    @Path("")
    public Response authResponse(
        @QueryParam(OAUTH2_PARAMETER_STATE) String state,
        @QueryParam(OAUTH2_PARAMETER_CODE) String authorizationCode,
        @QueryParam(ERROR) String error,
        @QueryParam(ERROR_DESCRIPTION) String errorDescription
    ) {
        String oldState = InfinispanUtils.get(state);

        return super.authResponse(oldState, authorizationCode, error, errorDescription);
    }

    @Override
    public SimpleHttp generateTokenRequest(String authorizationCode) {
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
