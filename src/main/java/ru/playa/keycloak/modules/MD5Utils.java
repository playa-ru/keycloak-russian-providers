package ru.playa.keycloak.modules;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Утилитный класс генерации хеша в формате MD5.
 *
 * @author Anatoliy Pokhresnyi
 */
public class MD5Utils {

    private static final char[] DIGITS_LOWER =
        {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Генерация хеша в формате MD5 для строки.
     * @param data Строка для котрой необходимо сгенерировать хеш.
     * @return Хеш.
     */
    public static String md5(final String data) {
        return new String(encode(md5(getBytes(data))));
    }

    private static byte[] getBytes(final String string) {
        if (string == null) {
            return null;
        }
        return string.getBytes(Charset.forName("UTF-8"));
    }

    private static byte[] md5(final byte[] data) {
        try {
            return MessageDigest.getInstance("MD5").digest(data);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static char[] encode(final byte[] data) {
        final int l = data.length;
        final char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = DIGITS_LOWER[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS_LOWER[0x0F & data[i]];
        }
        return out;
    }
}
