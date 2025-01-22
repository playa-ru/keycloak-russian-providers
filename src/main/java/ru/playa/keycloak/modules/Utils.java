package ru.playa.keycloak.modules;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;
import ru.playa.keycloak.modules.exception.NotHostedDomainException;


/**
 * Утилитный класс.
 *
 * @author Anatoliy Pokhresnyi
 */
public final class Utils {

    /**
     * ObjectMapper.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Минимальный размер случайной строки.
     */
    private static final int MIN_CODE_VERIFIER_ENTROPY = 128;

    /**
     * Шестнадцатеричная система счисления.
     */
    private static final char[] DIGITS_LOWER =
        {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Конструктор.
     */
    private Utils() {

    }

    /**
     * Проверка добавлен ли домен в белый список.
     *
     * @param email Электронная почта.
     * @param domains Белый список доменов.
     * @param provider Название провайдера.
     */
    public static void isHostedDomain(final String email, final String domains, final String provider) {
        final String domain = email.substring(email.indexOf("@") + 1);
        final boolean match = Optional
            .ofNullable(domains)
            .map(hd -> hd.split(","))
            .map(Arrays::asList)
            .orElse(Collections.singletonList("*"))
            .stream()
            .noneMatch(hd -> hd.equalsIgnoreCase(domain) || hd.equals("*"));

        if (match) {
            throw new NotHostedDomainException(provider, email);
        }
    }

    /**
     * Проверяет является ли строка пустой или null.
     *
     * @param value Строка которую необходимо проверить.
     * @return true - если строка пустая, false в противном случае.
     */
    public static boolean isNullOrEmpty(final String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Проверяет является ли строка не пустой и не равна null.
     *
     * @param value Строка которую необходимо проверить.
     * @return true - если строка не пустая, false в противном случае.
     */
    public static boolean nonNullOrEmpty(final String value) {
        return !isNullOrEmpty(value);
    }

    /**
     * Получает из спроки объект JsonNode.
     *
     * @param json Строка в формате Json.
     * @return JsonNode.
     */
    public static JsonNode asJsonNode(final String json) {
        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            return MAPPER.readTree(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * олучает значение поля по имени в формате JsonNode.
     *
     * @param node JsonNode.
     * @param field Название объекта.
     * @return JsonNode.
     */
    public static JsonNode asJsonNode(final JsonNode node, final String field) {
        return node == null ? null : node.get(field);
    }

    /**
     * Получает значение поля по имени в формате строки.
     *
     * @param node JsonNode.
     * @param field Название объекта.
     * @return Строка.
     */
    public static String asText(final JsonNode node, final String field) {
        return Optional
            .ofNullable(node)
            .map(it -> it.get(field))
            .map(JsonNode::asText)
            .orElse(null);
    }

    /**
     * Генерация сообщения ошибки на домен не из белого списка.
     *
     * @param provider Название провейдера
     * @param email    Почта пользователя
     * @return сообщение об ошибке
     */
    public static String toHostedDomainErrorMessage(final String provider, final String email) {
        return String.format(
            "Ваш аккаунт не подходит для авторизации через социальную сеть (%s) с почтой (%s).",
            provider,
            email);
    }

    /**
     * Получение случайной строки.
     *
     * @return Случайная строка
     */
    public static String getRandomString() {
        final SecureRandom entropySource = new SecureRandom();
        final byte[] randomBytes = new byte[MIN_CODE_VERIFIER_ENTROPY];
        entropySource.nextBytes(randomBytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Генерация хеша в формате MD5 для строки.
     *
     * @param data Строка для котрой необходимо сгенерировать хеш.
     * @return Хеш.
     */
    public static byte[] md5(final String data) {
        if (data == null) {
            return null;
        }

        final byte[] bytes = data.getBytes(StandardCharsets.UTF_8);

        try {
            return MessageDigest.getInstance("MD5").digest(bytes);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Преобразование байтового массива в HEX.
     *
     * @param data Байтового массива
     * @return HEX.
     */
    public static String hex(final byte[] data) {
        final int l = data.length;
        final char[] out = new char[l << 1];

        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = DIGITS_LOWER[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS_LOWER[0x0F & data[i]];
        }

        return new String(out);
    }


    /**
     * Генерация хеша в формате SHA-256 для строки.
     *
     * @param data Строка для котрой необходимо сгенерировать хеш.
     * @return Хеш.
     */
    public static String sha256(final String data) {
        try {
            final MessageDigest sha256Digester = MessageDigest.getInstance("SHA-256");
            final byte[] input = data.getBytes(StandardCharsets.ISO_8859_1);
            sha256Digester.update(input);

            final byte[] digestBytes = sha256Digester.digest();
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digestBytes);
        } catch (NoSuchAlgorithmException nsae) {
            return data;
        }
    }

}
