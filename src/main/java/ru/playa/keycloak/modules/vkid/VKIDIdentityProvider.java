package ru.playa.keycloak.modules.vkid;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.core.UriBuilder;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import ru.playa.keycloak.modules.AbstractRussianOAuth2IdentityProvider;
import ru.playa.keycloak.modules.InfinispanUtils;
import ru.playa.keycloak.modules.StringUtils;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.UUID;

/**
 * Провайдер OAuth-авторизации через <a href="https://vk.com">ВКонтакте</a>.
 * <a href="https://id.vk.com/about/business">Подробнее</a>.
 *
 * @author Anatoliy Pokhresnyi
 * @author dmitrymalinin
 */
public class VKIDIdentityProvider
    extends AbstractRussianOAuth2IdentityProvider<VKIDIdentityProviderConfig>
    implements SocialIdentityProvider<VKIDIdentityProviderConfig> {

    /**
     * Запрос кода подтверждения.
     */
    private static final String AUTH_URL = "https://id.vk.com/authorize";

    /**
     * Обмен кода подтверждения на токен.
     */
    private static final String TOKEN_URL = "https://id.vk.com/oauth2/auth";

    /**
     * Запрос информации о пользователе.
     */
    private static final String PROFILE_URL = "https://id.vk.com/oauth2/user_info";

    /**
     * Создает объект OAuth-авторизации через
     * <a href="https://vk.com">ВКонтакте</a>.
     *
     * @param session Сессия Keycloak.
     * @param config  Конфигурация OAuth-авторизации.
     */
    public VKIDIdentityProvider(KeycloakSession session, VKIDIdentityProviderConfig config) {
        super(session, config);

        config.setAuthorizationUrl(AUTH_URL);
        config.setTokenUrl(TOKEN_URL);
        config.setUserInfoUrl(PROFILE_URL);
    }

    @Override
    public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
        return new VKIDEndpoint(callback, event, this, session);
    }

    @Override
    public BrokeredIdentityContext getFederatedIdentity(String response) {
        logger.infof("GetFederatedIdentity %s", response);

        JsonNode node = StringUtils.asJsonNode(response);
        String accessToken = StringUtils.asText(node, "access_token");

        if (accessToken == null) {
            throw new IdentityBrokerException("No access token available in OAuth server response: " + response);
        }

        BrokeredIdentityContext biContext = doGetFederatedIdentity(accessToken);
        biContext.getContextData().put(FEDERATED_ACCESS_TOKEN, accessToken);
        return biContext;
    }

    /**
     * Запрос информации о пользователе.
     *
     * @return Данные авторизованного пользователя.
     */
    @Override
    protected BrokeredIdentityContext doGetFederatedIdentity(String accessToken) {
        try {
            logger.infof("DoGetFederatedIdentity AccessToken %s", accessToken);

            return extractIdentityFromProfile(
                    SimpleHttp
                            .doPost(getConfig().getUserInfoUrl(), session)
                            .param("access_token", accessToken)
                            .param("client_id", getConfig().getClientId())
                            .asJson()
            );
        } catch (IOException e) {
            throw new IdentityBrokerException("Could not obtain user profile from VK: " + e.getMessage(), e);
        }
    }

    protected BrokeredIdentityContext extractIdentityFromProfile(JsonNode node) {
        BrokeredIdentityContext user = extractIdentityFromProfile(null, node);

        String email = StringUtils.asText(node, "email");
        String phone = StringUtils.asText(node, "phone");

        if (getConfig().isEmailRequired() && StringUtils.isNullOrEmpty(email)) {
            throw new IllegalArgumentException(StringUtils.email("VK"));
        }

        if (StringUtils.nonNullOrEmpty(email)) {
            user.setUsername(email);
        } else {
            if (StringUtils.isNullOrEmpty(user.getUsername())) {
                user.setUsername("vk." + user.getId());
            }
        }

        user.setEmail(email);
        user.setUserAttribute("phone", phone);

        return user;
    }


    @Override
    protected BrokeredIdentityContext extractIdentityFromProfile(EventBuilder event, JsonNode node) {
        JsonNode context = StringUtils.asJsonNode(node, "user");
        BrokeredIdentityContext user = new BrokeredIdentityContext(
                Objects.requireNonNull(StringUtils.asText(context, "user_id")),
                getConfig()
        );

        user.setFirstName(StringUtils.asText(context, "first_name"));
        user.setLastName(StringUtils.asText(context, "last_name"));

        user.setIdp(this);

        AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, context, getConfig().getAlias());

        return user;
    }

    @Override
    protected UriBuilder createAuthorizationUrl(AuthenticationRequest request) {
        final String state = UUID.randomUUID().toString();
        final String code = StringUtils.generateRandomCodeVerifier(new SecureRandom());

        InfinispanUtils.put(state, request.getState().getEncoded());
        InfinispanUtils.put(request.getState().getEncoded(), code);

        return UriBuilder
                .fromUri(getConfig().getAuthorizationUrl())
                .queryParam(OAUTH2_PARAMETER_SCOPE, getConfig().getDefaultScope())
                .queryParam("state", state)
                .queryParam("code_challenge_method", "s256")
                .queryParam("code_challenge", StringUtils.deriveCodeVerifierChallenge(code))
                .queryParam(OAUTH2_PARAMETER_RESPONSE_TYPE, "code")
                .queryParam("client_id", getConfig().getClientId())
                .queryParam(OAUTH2_PARAMETER_REDIRECT_URI, request.getRedirectUri());
    }

    @Override
    protected String getDefaultScopes() {
        return "";
    }
}
