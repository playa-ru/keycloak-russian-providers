package ru.playa.keycloak.modules.yandex;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.core.UriBuilder;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.http.simple.SimpleHttp;
import org.keycloak.http.simple.SimpleHttpRequest;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import ru.playa.keycloak.modules.AbstractRussianOAuth2IdentityProvider;
import ru.playa.keycloak.modules.Utils;

import java.io.IOException;
import java.util.List;

/**
 * Провайдер OAuth-авторизации через <a href="https://yandex.ru">Яндекс</a>.
 * <a href="https://tech.yandex.ru/oauth/">Подробнее</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class YandexIdentityProvider
        extends AbstractRussianOAuth2IdentityProvider<YandexIdentityProviderConfig>
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
    protected SimpleHttpRequest buildUserInfoRequest(final String subjectToken, final String userInfoUrl) {
        return SimpleHttp.create(session).doGet(PROFILE_URL + "?oauth_token=" + subjectToken);
    }

    @Override
    protected BrokeredIdentityContext extractIdentityFromProfile(final EventBuilder event, final JsonNode node) {
        BrokeredIdentityContext user = new BrokeredIdentityContext(getJsonProperty(node, "id"), getConfig());

        String email = getJsonProperty(node, "default_email");
        if (!Utils.isNullOrEmpty(email)) {
            user.setEmail(email);
            Utils.isHostedDomain(email, getConfig().getHostedDomain(), YandexIdentityProviderFactory.PROVIDER_ID);
        }

        String login = getJsonProperty(node, "login");
        if (Utils.isNullOrEmpty(login)) {
            login = email;
        }
        if (Utils.isNullOrEmpty(login)) {
            login = "ya." + user.getId();
        }

        String phone = null;
        JsonNode defaultPhone = node.get("default_phone");
        if (defaultPhone != null && !defaultPhone.isNull()) {
            var numberNode = defaultPhone.get("number");
            if (numberNode != null && !numberNode.isNull()) {
                phone = numberNode.asText();
            }
        }

        if (getConfig().isPhoneRequired() && phone == null) {
            boolean isNewUser = session.users().getUserByFederatedIdentity(
                    session.getContext().getRealm(),
                    new FederatedIdentityModel(getConfig().getAlias(), user.getId(), null)
            ) == null;

            if (isNewUser) {
                throw new IdentityBrokerException("Phone number is required for Yandex registration")
                        .withMessageCode("identityProviderPhoneRequiredMessage");
            }
        }
        var phoneAttribute = getConfig().phoneNumberAttribute();
        if (phoneAttribute != null && !phoneAttribute.isEmpty()) {
            user.setUserAttribute(phoneAttribute, phone);
        }

        user.setUsername(login);
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
                            .create(session)
                            .doGet(PROFILE_URL + "?oauth_token=" + accessToken)
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
    public void updateBrokeredUser(
            final KeycloakSession session,
            final RealmModel realm,
            final UserModel user,
            final BrokeredIdentityContext context
    ) {
        super.updateBrokeredUser(session, realm, user, context);
        updatePhoneAttribute(user, context);
    }

    /**
     * Обновляет атрибут номера телефона пользователя из контекста брокерской аутентификации.
     *
     * @param user    Пользователь Keycloak.
     * @param context Контекст брокерской аутентификации.
     */
    private void updatePhoneAttribute(final UserModel user, final BrokeredIdentityContext context) {
        String phoneAttribute = getConfig().phoneNumberAttribute();
        if (phoneAttribute == null || phoneAttribute.isEmpty()) {
            return;
        }
        String phone = context.getUserAttribute(phoneAttribute);
        if (phone != null) {
            user.setAttribute(phoneAttribute, List.of(phone));
        } else {
            user.removeAttribute(phoneAttribute);
        }
    }

    @Override
    protected String getDefaultScopes() {
        return DEFAULT_SCOPE;
    }
}
