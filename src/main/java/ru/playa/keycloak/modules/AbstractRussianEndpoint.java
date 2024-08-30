package ru.playa.keycloak.modules;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.OAuthErrorException;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.broker.provider.util.IdentityBrokerState;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.ErrorPage;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.ClientSessionCode;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

import static org.keycloak.OAuth2Constants.CODE_VERIFIER;
import static org.keycloak.OAuth2Constants.ERROR;
import static org.keycloak.OAuth2Constants.ERROR_DESCRIPTION;
import static org.keycloak.OAuth2Constants.STATE;
import static org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider.ACCESS_DENIED;
import static org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE;
import static org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_CODE;
import static org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_GRANT_TYPE;
import static org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_REDIRECT_URI;
import static org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_STATE;

/**
 * Переопределенный класс {@code AbstractOAuth2IdentityProvider#Endpoint}.
 * Класс переопределен с целью возвращения человеко-читаемой ошибки если
 * в профиле социальной сети не указана электронная почта.
 */
public class AbstractRussianEndpoint {
    private static final String BROKER_CODE_CHALLENGE_PARAM = "BROKER_CODE_CHALLENGE";
    protected static final Logger LOGGER = Logger.getLogger(AbstractOAuth2IdentityProvider.class);

    private final IdentityProvider.AuthenticationCallback callback;
    private final EventBuilder event;
    private final AbstractOAuth2IdentityProvider provider;
    private final KeycloakSession session;

    public AbstractRussianEndpoint(
        final IdentityProvider.AuthenticationCallback aCallback,
        final EventBuilder aEvent,
        final AbstractOAuth2IdentityProvider aProvider,
        final KeycloakSession sSession
    ) {
        this.callback = aCallback;
        this.event = aEvent;
        this.provider = aProvider;
        this.session = sSession;
    }

    @GET
    @Path("")
    public Response authResponse(
        @QueryParam(OAUTH2_PARAMETER_STATE) final String state,
        @QueryParam(OAUTH2_PARAMETER_CODE) final String authorizationCode,
        @QueryParam(ERROR) final String error,
        @QueryParam(ERROR_DESCRIPTION) final String errorDescription
    ) {
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
                if (error.equals(ACCESS_DENIED)) {
                    return callback.cancelled(providerConfig);
                } else if (error.equals(OAuthErrorException.LOGIN_REQUIRED) || error.equals(
                    OAuthErrorException.INTERACTION_REQUIRED)) {
                    return callback.error(error);
                } else if (error.equals(
                    OAuthErrorException.TEMPORARILY_UNAVAILABLE) && Constants.AUTHENTICATION_EXPIRED_MESSAGE.equals(
                    errorDescription)) {
                    return callback.retryLogin(this.provider, authSession);
                } else {
                    return callback.error(Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
                }
            }

            if (authorizationCode == null) {
                logErroneousRedirectUrlError(
                    "Redirection URL neither contains a code nor error parameter",
                    providerConfig
                );
                return errorIdentityProviderLogin(Messages.IDENTITY_PROVIDER_MISSING_CODE_OR_ERROR_ERROR);
            }

            SimpleHttp simpleHttp = generateTokenRequest(authorizationCode);
            String response;
            try (SimpleHttp.Response simpleResponse = simpleHttp.asResponse()) {
                int status = simpleResponse.getStatus();
                boolean success = status >= 200 && status < 400;
                response = simpleResponse.asString();

                if (!success) {
                    LOGGER.errorf("Unexpected response from token endpoint %s. status=%s, response=%s",
                                  simpleHttp.getUrl(), status, response
                    );
                    return errorIdentityProviderLogin(Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
                }
            }

            BrokeredIdentityContext federatedIdentity = provider.getFederatedIdentity(response);

            if (providerConfig.isStoreToken()) {
                // make sure that token wasn't already set by getFederatedIdentity();
                // want to be able to allow provider to set the token itself.
                if (federatedIdentity.getToken() == null) {
                    federatedIdentity.setToken(response);
                }
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
            LOGGER.error("Failed to make identity provider oauth callback", e);
            return errorIdentityProviderLogin(Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
        } catch (Exception e) {
            LOGGER.error("Failed to make identity provider oauth callback", e);
            return errorIdentityProviderLogin(Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
        }
    }

    private void logErroneousRedirectUrlError(String mainMessage, OAuth2IdentityProviderConfig providerConfig) {
        String providerId = providerConfig.getProviderId();
        String redirectionUrl = session.getContext().getUri().getRequestUri().toString();

        LOGGER.errorf("%s. providerId=%s, redirectionUrl=%s", mainMessage, providerId, redirectionUrl);
    }

    private Response errorIdentityProviderLogin(String message) {
        event.event(EventType.IDENTITY_PROVIDER_LOGIN);
        event.error(Errors.IDENTITY_PROVIDER_LOGIN_FAILURE);
        return ErrorPage.error(session, null, Response.Status.BAD_GATEWAY, message);
    }

    public SimpleHttp generateTokenRequest(String authorizationCode) {
        KeycloakContext context = session.getContext();
        OAuth2IdentityProviderConfig providerConfig = provider.getConfig();
        SimpleHttp tokenRequest = SimpleHttp
            .doPost(providerConfig.getTokenUrl(), session)
            .param(OAUTH2_PARAMETER_CODE, authorizationCode)
            .param(
                OAUTH2_PARAMETER_REDIRECT_URI,
                Urls
                    .identityProviderAuthnResponse(
                        context.getUri().getBaseUri(), providerConfig.getAlias(), context.getRealm().getName())
                    .toString()
            )
            .param(OAUTH2_PARAMETER_GRANT_TYPE, OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE);

        if (providerConfig.isPkceEnabled()) {

            // reconstruct the original code verifier that was used to generate the code challenge from the HttpRequest.
            String stateParam = session.getContext().getUri().getQueryParameters().getFirst(STATE);
            if (stateParam == null) {
                LOGGER.warn("Cannot lookup PKCE code_verifier: state param is missing.");
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
                AuthenticationSessionModel.class
            );

            if (authSession == null) {
                LOGGER.warnf("Cannot lookup PKCE code_verifier: authSession not found. state=%s", stateParam);
                return tokenRequest;
            }

            String brokerCodeChallenge = authSession.getClientNote(BROKER_CODE_CHALLENGE_PARAM);
            if (brokerCodeChallenge == null) {
                LOGGER.warnf("Cannot lookup PKCE code_verifier: brokerCodeChallenge not found. state=%s", stateParam);
                return tokenRequest;
            }

            tokenRequest.param(CODE_VERIFIER, brokerCodeChallenge);
        }

        return provider.authenticateTokenRequest(tokenRequest);
    }

}

