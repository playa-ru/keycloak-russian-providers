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

    /**
     * Получает значение указанного поля.
     *
     * @param json  Строка в формате Json.
     * @param field Название поля значение, которого необходимо получить.
     * @return Значение выбранного поля.
     */
    public static String getAsString(String json, String field) {
        try {
            JsonNode root = new ObjectMapper().readTree(json);

            if (root == null) {
                return null;
            }

            JsonNode node = root.get(field);

            return node == null ? null : node.asText();
        } catch (IOException e) {
            return null;
        }
    }
}
