package ru.playa.keycloak.modules;

/**
 * Ошибка которая произошла во время логина через ЕСИА.
 *
 * @author Anatoliy Pokhresnyi
 */
public class RussianException extends RuntimeException {

    /**
     * Код ошибки "Разрешенные доменные адреса".
     */
    public static final String HOSTED_DOMAIN_KEY = "HostedDomainErrorMessage";

    /**
     * Код ошибки "Не удалось получить электронную почту".
     */
    public static final String EMAIL_CAN_NOT_EMPTY_KEY = "vk-email-can-not-empty";

    /**
     * Код ошибки.
     */
    private final String code;

    /**
     * Конструктор.
     *
     * @param aProvider Код провайдера
     * @param aCode     Код ошибки.
     */
    public RussianException(final String aProvider, final String aCode) {
        this(aProvider, aCode, null);
    }

    /**
     * Конструктор.
     *
     * @param aEmail    Электронная почта пользователя
     * @param aProvider Код провайдера
     * @param aCode     Код ошибки.
     */
    public RussianException(final String aProvider, final String aCode, final String aEmail) {
        super(toMessage(aProvider, aCode, aEmail));

        this.code = aCode;
    }

    /**
     * Получает код ошибки.
     *
     * @return Код ошибки.
     */
    public String getKey() {
        return code;
    }

    /**
     * Геренации сообщения об ошибке.
     *
     * @param aProvider Провайдер.
     * @param aCode Код.
     * @param aEmail Электронная почта.
     * @return Сообщение об ошибке
     */
    private static String toMessage(final String aProvider, final String aCode, final String aEmail) {
        if (EMAIL_CAN_NOT_EMPTY_KEY.equals(aCode)) {
            return String.format(
                "Для авторизации через социальную сеть (%s) необходимо в Вашем профиле соцсети указать Ваш e-mail.",
                aProvider
            );
        }

        if (HOSTED_DOMAIN_KEY.equals(aCode)) {
            return String.format(
                "Ваш аккаунт не подходит для авторизации через социальную сеть (%s) с почтой (%s).",
                aProvider,
                aEmail);
        }

        return aCode;
    }
}