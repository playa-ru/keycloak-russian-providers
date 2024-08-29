package ru.playa.keycloak.modules.vkid;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.OAuth2Constants;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.common.ClientConnection;
import org.keycloak.events.EventBuilder;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.Urls;
import ru.playa.keycloak.modules.*;

import java.util.UUID;

/**
 * Переопределенный класс {@link AbstractOAuth2IdentityProvider.Endpoint}.
 * Класс переопределен с целью возвращения человеко-читаемой ошибки если
 * в профиле социальной сети не указана электронная почта.
 */
public class VkEndpoint extends AEndpoint {

    protected static final Logger logger = Logger.getLogger(AbstractOAuth2IdentityProvider.class);

    private final VKIDIdentityProvider provider;
    private final KeycloakSession session;
    private final HttpRequest httpRequest;

    public VkEndpoint(
            IdentityProvider.AuthenticationCallback callback,
            RealmModel realm,
            EventBuilder event,
            VKIDIdentityProvider provider,
            KeycloakSession session
    ) {
        super(callback, realm, event, provider, session);
        this.provider = provider;
        this.session = provider.getSession();
        this.httpRequest = session.getContext().getHttpRequest();
    }

    @GET
    @Path("")
    public Response authResponse(
            @QueryParam(AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_STATE) String state,
            @QueryParam(AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_CODE) String authorizationCode,
            @QueryParam(OAuth2Constants.ERROR) String error,
            @QueryParam(OAuth2Constants.ERROR_DESCRIPTION) String errorDescription
    ) {
        String oldState = InfinispanUtils.get(state);

        return super.authResponse(oldState, authorizationCode, error, errorDescription);
    }

    public SimpleHttp generateTokenRequest(String authorizationCode) {
        String device_id = httpRequest.getUri().getQueryParameters().getFirst("device_id");
        String state = httpRequest.getUri().getQueryParameters().getFirst("state");
        String oldState = InfinispanUtils.get(state);
        String codeVerifier = InfinispanUtils.get(oldState);

        logger.infof("VkEndpoint CreateAuthorizationUrl code_challenge %s", MD5Utils.sha256(codeVerifier));
        logger.infof("VkEndpoint CreateAuthorizationUrl code_verifier %s", codeVerifier);
        logger.infof("VkEndpoint CreateAuthorizationUrl device_id %s", device_id);
        logger.infof("VkEndpoint CreateAuthorizationUrl state %s", state);
        logger.infof("VkEndpoint CreateAuthorizationUrl authorization_code %s", authorizationCode);

        SimpleHttp tokenRequest = SimpleHttp
                .doPost(provider.getConfig().getTokenUrl(), session)
                .param(AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_CODE, authorizationCode)
                .param(AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_REDIRECT_URI, Urls.identityProviderAuthnResponse(provider.getSession().getContext().getUri().getBaseUri(),
                        provider.getConfig().getAlias(), provider.getSession().getContext().getRealm().getName()).toString())
                .param(AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_GRANT_TYPE, AbstractOAuth2IdentityProvider.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE)
                .param(AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_CLIENT_ID, provider.getConfig().getClientId())
                .param("device_id", device_id)
                .param("code_verifier",codeVerifier)
                .param("state", state);

        logger.infof("CreateAuthorizationUrl %s", tokenRequest.getUrl());

        return tokenRequest;
    }
}
