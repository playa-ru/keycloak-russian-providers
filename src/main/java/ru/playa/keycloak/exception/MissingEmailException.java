package ru.playa.keycloak.exception;

import org.keycloak.broker.provider.IdentityBrokerException;

/**
 * Ошибка которая произошла во время логина через ЕСИА: не указан эмейл.
 *
 * @author Artur Dombrovskii
 */
public class MissingEmailException extends IdentityBrokerException {

  /**
   * @param providerName Имя провайдера.
   */
  public MissingEmailException(final String providerName) {
    super(String.format("Missing email for provider %s.", providerName));
    this.withMessageCode(String.format(
        "Для авторизации через социальную сеть (%s) необходимо в Вашем профиле соцсети указать Ваш e-mail.",
        providerName
    ));
  }

}
