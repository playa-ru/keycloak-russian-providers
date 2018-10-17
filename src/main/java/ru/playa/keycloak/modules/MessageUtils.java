package ru.playa.keycloak.modules;

/**
 * Утилитный класс генерации сообщений ошибок, при регистрации через
 * социальные сети.
 *
 * @author Anatoliy Pokhresnyi
 */
public class MessageUtils {

    /**
     * Сообщение об ошибке "Для авторизации через социальную сеть необходимо
     * в Вашем профиле соцсети указать Ваш e-mail.".
     */
    public static final String EMAIL = "identityProviderEmailErrorMessage";

    /**
     * Генерация сообщения ошибки.
     *
     * @param provider Название провейдера
     * @return сообщение об ошибке
     */
    public static String email(String provider) {
        return String.format("Для авторизации через социальную сеть (%s) необходимо в Вашем профиле соцсети указать Ваш e-mail.", provider);
    }
}
