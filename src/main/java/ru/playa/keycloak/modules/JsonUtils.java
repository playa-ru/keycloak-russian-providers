package ru.playa.keycloak.modules;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Утилитный класс парсинга json.
 *
 * @author Anatoliy Pokhresnyi
 */
public class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static JsonNode asJsonNode(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            return MAPPER.readTree(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode asJsonNode(JsonNode node, String field) {
        return node == null ? null : node.get(field);

    }

    public static String asText(JsonNode node, String field) {
        return node == null ? null : node.get(field).asText();

    }

    /**
     * Получает значение указанного поля.
     *
     * @param json  Строка в формате Json.
     * @param field Название поля значение, которого необходимо получить.
     * @return Значение выбранного поля.
     */

}
