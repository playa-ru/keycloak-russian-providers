package ru.playa.keycloak.modules;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;

/**
 * Утилитный класс.
 *
 * @author Anatoliy Pokhresnyi
 */
public class StringUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ALGORITHM = "SHA-256";
    private static final String CHARSET_NAME = "ISO-8859-1";
    private static final int MIN_CODE_VERIFIER_ENTROPY = 128;
    private static final char[] DIGITS_LOWER =
        {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Проверка добавлен ли домен в белый список.
     *
     * @param email Электронная почта.
     * @param domains Белый список доменов.
     * @param provider Название провайдера.
     */
    public static void isHostedDomain(final String email, final String domains, final String provider) {
        String domain = email.substring(email.indexOf("@") + 1);
        boolean match = Optional
            .ofNullable(domains)
            .map(hd -> hd.split(","))
            .map(Arrays::asList)
            .orElse(Collections.singletonList("*"))
            .stream()
            .noneMatch(hd -> hd.equalsIgnoreCase(domain) || hd.equals("*"));

        if (match) {
            throw new IllegalArgumentException(StringUtils.hostedDomain(provider, domain));
        }
    }

    /**
     * Проверяет является ли строка пустой или null.
     *
     * @param value Строка которую необходимо проверить.
     * @return true - если строка пустая, false в противном случае.
     */
    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Проверяет является ли строка не пустой и не равна null.
     *
     * @param value Строка которую необходимо проверить.
     * @return true - если строка не пустая, false в противном случае.
     */
    public static boolean nonNullOrEmpty(String value) {
        return !isNullOrEmpty(value);
    }



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
        return Optional
            .ofNullable(node)
            .map(it -> it.get(field))
            .map(JsonNode::asText)
            .orElse(null);
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
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);

        try {
            return MessageDigest.getInstance("MD5").digest(bytes);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String hex(final byte[] data) {
        final int l = data.length;
        final char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = DIGITS_LOWER[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS_LOWER[0x0F & data[i]];
        }
        return new String(out);
    }

    /**
     * Генерация сообщения ошибки.
     *
     * @param provider Название провейдера
     * @return сообщение об ошибке
     */
    public static String email(String provider) {
        return String.format(
            "Для авторизации через социальную сеть (%s) необходимо в Вашем профиле соцсети указать Ваш e-mail.",
            provider);
    }

    /**
     * Генерация сообщения ошибки на домен не из белого списка.
     *
     * @param provider Название провейдера
     * @param email    Почта пользователя
     * @return сообщение об ошибке
     */
    public static String hostedDomain(String provider, String email) {
        return String.format(
            "Ваш аккаунт не подходит для авторизации через социальную сеть (%s) с почтой (%s).",
            provider,
            email);
    }

    public static String generateRandomCodeVerifier(SecureRandom entropySource) {
        byte[] randomBytes = new byte[MIN_CODE_VERIFIER_ENTROPY];
        entropySource.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public static String deriveCodeVerifierChallenge(String codeVerifier) {
        try {
            MessageDigest sha256Digester = MessageDigest.getInstance(ALGORITHM);
            byte[] input = codeVerifier.getBytes(Charset.forName(CHARSET_NAME));
            sha256Digester.update(input);
            byte[] digestBytes = sha256Digester.digest();
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digestBytes);
        } catch (NoSuchAlgorithmException nsae) {
            return codeVerifier;
        }
    }


}
