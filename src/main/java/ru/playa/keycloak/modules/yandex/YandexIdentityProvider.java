package ru.playa.keycloak.modules.yandex;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.core.UriBuilder;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import ru.playa.keycloak.modules.Utils;

import java.io.IOException;
import ru.playa.keycloak.modules.exception.MissingEmailException;


/**
 * Провайдер OAuth-авторизации через <a href="https://yandex.ru">Яндекс</a>.
 * <a href="https://tech.yandex.ru/oauth/">Подробнее</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class YandexIdentityProvider
    extends AbstractOAuth2IdentityProvider<YandexIdentityProviderConfig>
    implements SocialIdentityProvider<YandexIdentityProviderConfig> {

    /**
     * Запрос кода подтверждения.
     */
    private static final String AUTH_URL = "https://oauth.yandex.ru/authorize";

    /**
     * Обмен кода подтверждения на токен.
     */
    private static final String TOKEN_URL = "https://oauth.yandex.ru/token";

    /**
     * Запрос информации о пользователе.
     */
    private static final String PROFILE_URL = "https://login.yandex.ru/info";

    /**
     * Права доступа к данным пользователя по умолчанию.
     */
    private static final String DEFAULT_SCOPE = "";

    /**
     * Создает объект OAuth-авторизации через
     * <a href="https://yandex.ru">Яндекс</a>.
     *
     * @param session Сессия Keycloak.
     * @param config  Конфигурация OAuth-авторизации.
     */
    public YandexIdentityProvider(final KeycloakSession session, final YandexIdentityProviderConfig config) {
        super(session, config);
        config.setAuthorizationUrl(AUTH_URL);
        config.setTokenUrl(TOKEN_URL);
        config.setUserInfoUrl(PROFILE_URL);
    }

    @Override
    protected boolean supportsExternalExchange() {
        return true;
    }

    @Override
    protected String getProfileEndpointForValidation(final EventBuilder event) {
        return PROFILE_URL;
    }

    @Override
    protected SimpleHttp buildUserInfoRequest(final String subjectToken, final String userInfoUrl) {
        return SimpleHttp.doGet(PROFILE_URL + "?oauth_token=" + subjectToken, session);
    }

    @Override
    protected BrokeredIdentityContext extractIdentityFromProfile(final EventBuilder event, final JsonNode node) {
        BrokeredIdentityContext user = new BrokeredIdentityContext(getJsonProperty(node, "id"), getConfig());

        String email = getJsonProperty(node, "default_email");
        if (Utils.isNullOrEmpty(email)) {
            throw new MissingEmailException(YandexIdentityProviderFactory.PROVIDER_ID);
        } else {
            Utils.isHostedDomain(email, getConfig().getHostedDomain(), YandexIdentityProviderFactory.PROVIDER_ID);
        }

        String login = getJsonProperty(node, "login");
        if (Utils.isNullOrEmpty(login)) {
            user.setUsername(email);
        } else {
            user.setUsername(login);
        }

        user.setEmail(email);
        user.setLastName(getJsonProperty(node, "last_name"));
        user.setFirstName(getJsonProperty(node, "first_name"));

        user.setIdp(this);

        AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, node, getConfig().getAlias());

        return user;
    }

    @Override
    protected BrokeredIdentityContext doGetFederatedIdentity(final String accessToken) {
        try {
            return extractIdentityFromProfile(
                null,
                SimpleHttp
                    .doGet(PROFILE_URL + "?oauth_token=" + accessToken, session)
                    .asJson()
            );
        } catch (IOException e) {
            throw new IdentityBrokerException("Could not obtain user profile from Yandex: " + e.getMessage(), e);
        }
    }

    @Override
    protected UriBuilder createAuthorizationUrl(final AuthenticationRequest request) {
        UriBuilder builder = super.createAuthorizationUrl(request);
        if (getConfig().isForceConfirm()) {
            builder.queryParam("force_confirm", "yes");
        }
        return builder;
    }


    @Override
    protected String getDefaultScopes() {
        return DEFAULT_SCOPE;
    }
}