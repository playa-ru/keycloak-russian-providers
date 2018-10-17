package ru.playa.keycloak.modules;

/**
 * Утилитный класс.
 *
 * @author Anatoliy Pokhresnyi
 */
public class StringUtils {

    /**
     * Проверяет является ли строка пустой или null.
     *
     * @param value Строка которую необходимо проверить.
     * @return true - если строка пустая, false в противном случае.
     */
    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
