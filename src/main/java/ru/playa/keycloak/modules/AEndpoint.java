package ru.playa.keycloak.modules;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.OAuth2Constants;
import org.keycloak.OAuthErrorException;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.broker.provider.util.IdentityBrokerState;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.common.ClientConnection;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.*;
import org.keycloak.services.ErrorPage;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.ClientSessionCode;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

public class AEndpoint {
    private static final String BROKER_CODE_CHALLENGE_PARAM = "BROKER_CODE_CHALLENGE";
    private static final String BROKER_CODE_CHALLENGE_METHOD_PARAM = "BROKER_CODE_CHALLENGE_METHOD";

    protected static final Logger logger = Logger.getLogger(AbstractOAuth2IdentityProvider.class);

    protected final IdentityProvider.AuthenticationCallback callback;
    protected final RealmModel realm;
    protected final EventBuilder event;
    private final AbstractOAuth2IdentityProvider provider;

    protected final KeycloakSession session;

    protected final ClientConnection clientConnection;

    protected final HttpHeaders headers;

    protected final HttpRequest httpRequest;

    public AEndpoint(
            IdentityProvider.AuthenticationCallback callback,
            RealmModel realm,
            EventBuilder event,
            AbstractOAuth2IdentityProvider provider,
            KeycloakSession session
    ) {
        this.callback = callback;
        this.realm = realm;
        this.event = event;
        this.provider = provider;
        this.session = session;
        this.clientConnection = session.getContext().getConnection();
        this.httpRequest = session.getContext().getHttpRequest();
        this.headers = session.getContext().getRequestHeaders();
    }

    @GET
    public Response authResponse(@QueryParam(AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_STATE) String state,
                                 @QueryParam(AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_CODE) String authorizationCode,
                                 @QueryParam(OAuth2Constants.ERROR) String error,
                                 @QueryParam(OAuth2Constants.ERROR_DESCRIPTION) String errorDescription) {
        OAuth2IdentityProviderConfig providerConfig = provider.getConfig();

        if (state == null) {
            logErroneousRedirectUrlError("Redirection URL does not contain a state parameter", providerConfig);
            return errorIdentityProviderLogin(Messages.IDENTITY_PROVIDER_MISSING_STATE_ERROR);
        }

        try {
            AuthenticationSessionModel authSession = this.callback.getAndVerifyAuthenticationSession(state);
            session.getContext().setAuthenticationSession(authSession);

            if (error != null) {
                logErroneousRedirectUrlError("Redirection URL contains an error", providerConfig);
                if (error.equals(AbstractOAuth2IdentityProvider.ACCESS_DENIED)) {
                    return callback.cancelled(providerConfig);
                } else if (error.equals(OAuthErrorException.LOGIN_REQUIRED) || error.equals(OAuthErrorException.INTERACTION_REQUIRED)) {
                    return callback.error(error);
                } else if (error.equals(OAuthErrorException.TEMPORARILY_UNAVAILABLE) && Constants.AUTHENTICATION_EXPIRED_MESSAGE.equals(errorDescription)) {
                    return callback.retryLogin(this.provider, authSession);
                } else {
                    return callback.error(Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
                }
            }

            if (authorizationCode == null) {
                logErroneousRedirectUrlError("Redirection URL neither contains a code nor error parameter",
                        providerConfig);
                return errorIdentityProviderLogin(Messages.IDENTITY_PROVIDER_MISSING_CODE_OR_ERROR_ERROR);
            }

            SimpleHttp simpleHttp = generateTokenRequest(authorizationCode);
            String response;
            try (SimpleHttp.Response simpleResponse = simpleHttp.asResponse()) {
                int status = simpleResponse.getStatus();
                boolean success = status >= 200 && status < 400;
                response = simpleResponse.asString();

                if (!success) {
                    logger.errorf("Unexpected response from token endpoint %s. status=%s, response=%s",
                            simpleHttp.getUrl(), status, response);
                    return errorIdentityProviderLogin(Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
                }
            }

            BrokeredIdentityContext federatedIdentity = provider.getFederatedIdentity(response);

            if (providerConfig.isStoreToken()) {
                // make sure that token wasn't already set by getFederatedIdentity();
                // want to be able to allow provider to set the token itself.
                if (federatedIdentity.getToken() == null)federatedIdentity.setToken(response);
            }

            federatedIdentity.setIdp(provider);
            federatedIdentity.setAuthenticationSession(authSession);

            return callback.authenticated(federatedIdentity);
        } catch (WebApplicationException e) {
            return e.getResponse();
        } catch (IdentityBrokerException e) {
            if (e.getMessageCode() != null) {
                return errorIdentityProviderLogin(e.getMessageCode());
            }
            logger.error("Failed to make identity provider oauth callback", e);
            return errorIdentityProviderLogin(Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
        } catch (Exception e) {
            logger.error("Failed to make identity provider oauth callback", e);
            return errorIdentityProviderLogin(Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
        }
    }

    private void logErroneousRedirectUrlError(String mainMessage, OAuth2IdentityProviderConfig providerConfig) {
        String providerId = providerConfig.getProviderId();
        String redirectionUrl = session.getContext().getUri().getRequestUri().toString();

        logger.errorf("%s. providerId=%s, redirectionUrl=%s", mainMessage, providerId, redirectionUrl);
    }

    private Response errorIdentityProviderLogin(String message) {
        event.event(EventType.IDENTITY_PROVIDER_LOGIN);
        event.error(Errors.IDENTITY_PROVIDER_LOGIN_FAILURE);
        return ErrorPage.error(session, null, Response.Status.BAD_GATEWAY, message);
    }

    public SimpleHttp generateTokenRequest(String authorizationCode) {
        KeycloakContext context = session.getContext();
        OAuth2IdentityProviderConfig providerConfig = provider.getConfig();
        SimpleHttp tokenRequest = SimpleHttp.doPost(providerConfig.getTokenUrl(), session)
                .param(AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_CODE, authorizationCode)
                .param(AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_REDIRECT_URI, Urls.identityProviderAuthnResponse(context.getUri().getBaseUri(),
                        providerConfig.getAlias(), context.getRealm().getName()).toString())
                .param(AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_GRANT_TYPE, AbstractOAuth2IdentityProvider.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE);

        if (providerConfig.isPkceEnabled()) {

            // reconstruct the original code verifier that was used to generate the code challenge from the HttpRequest.
            String stateParam = session.getContext().getUri().getQueryParameters().getFirst(OAuth2Constants.STATE);
            if (stateParam == null) {
                logger.warn("Cannot lookup PKCE code_verifier: state param is missing.");
                return tokenRequest;
            }

            RealmModel realm = context.getRealm();
            IdentityBrokerState idpBrokerState = IdentityBrokerState.encoded(stateParam, realm);
            ClientModel client = realm.getClientByClientId(idpBrokerState.getClientId());

            AuthenticationSessionModel authSession = ClientSessionCode.getClientSession(
                    idpBrokerState.getEncoded(),
                    idpBrokerState.getTabId(),
                    session,
                    realm,
                    client,
                    event,
                    AuthenticationSessionModel.class);

            if (authSession == null) {
                logger.warnf("Cannot lookup PKCE code_verifier: authSession not found. state=%s", stateParam);
                return tokenRequest;
            }

            String brokerCodeChallenge = authSession.getClientNote(BROKER_CODE_CHALLENGE_PARAM);
            if (brokerCodeChallenge == null) {
                logger.warnf("Cannot lookup PKCE code_verifier: brokerCodeChallenge not found. state=%s", stateParam);
                return tokenRequest;
            }

            tokenRequest.param(OAuth2Constants.CODE_VERIFIER, brokerCodeChallenge);
        }

        return provider.authenticateTokenRequest(tokenRequest);
    }

}

