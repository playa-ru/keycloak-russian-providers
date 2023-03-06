package ru.playa.keycloak.modules;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Базовый класс описывающий пользовательские аттрибуты необходимые для авторизации.
 * Данный класс описывает аттрибуты через json-path.
 *
 * @author Anatoliy Pokhresnyi
 */
public abstract class AbstractRussianJsonUserAttributeMapper extends AbstractJsonUserAttributeMapper {

    @Override
    public void preprocessFederatedIdentity(
        final KeycloakSession session,
        final RealmModel realm,
        final IdentityProviderMapperModel mapper,
        final BrokeredIdentityContext context
    ) {
        parse(mapper, context);
    }

    @Override
    public void updateBrokeredUser(
        final KeycloakSession session,
        final RealmModel realm,
        final UserModel user,
        final IdentityProviderMapperModel mapper,
        final BrokeredIdentityContext context
    ) {
        parse(mapper, context);
    }

    /**
     * Парсинг данных пользователе атрибут маппером.
     *
     * @param mapper Атрибут маппер.
     * @param context Данные о пользователе.
     */
    private static void parse(
        final IdentityProviderMapperModel mapper,
        final BrokeredIdentityContext context
    ) {
        final String attribute = Optional
            .ofNullable(mapper.getConfig().get(CONF_USER_ATTRIBUTE))
            .map(String::trim)
            .filter(value -> !value.isEmpty())
            .orElseThrow(() -> {
                throw new IllegalArgumentException("Attribute name is not set");
            });
        final JsonPath path = Optional
            .ofNullable(mapper.getConfig().get(CONF_JSON_FIELD))
            .map(String::trim)
            .map(value -> value.startsWith("$.") ? value : "$." +  value)
            .map(JsonPath::compile)
            .orElseThrow(() -> {
                throw new IllegalArgumentException("Json path to the object is not set");
            });
        final String json = Optional
            .ofNullable((JsonNode) context.getContextData().get(CONTEXT_JSON_NODE))
            .map(JsonNode::toString)
            .orElse(null);
        final List<String> values = Optional
            .ofNullable(parse(json, path))
            .map(value -> {
                if (value instanceof JSONArray) {
                    return ((JSONArray) value)
                        .stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());
                } else {
                    return Collections.singleton(value.toString());
                }
            })
            .orElse(Collections.emptyList())
            .stream()
            .map(String::trim)
            .filter(value -> !value.isEmpty())
            .map(value -> value.length() > 250 ? value.substring(0, 250) : value)
            .collect(Collectors.toList());

        context.removeUserAttribute(attribute);
        context.setUserAttribute(attribute, values);
    }

    /**
     * Парсинг Json.
     *
     * @param json Json из ЕСИА.
     * @param path Путь до элемента.
     * @return Элемент.
     */
    private static Object parse(final String json, final JsonPath path) {
        try {
            return JsonPath.parse(json).read(path);
        } catch (Exception exception) {
            return null;
        }
    }

}
