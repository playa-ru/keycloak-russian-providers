package ru.playa.keycloak.modules.exception;

import org.keycloak.broker.provider.IdentityBrokerException;

/**
 * Ошибка которая произошла во время логина через ЕСИА: не разрешенный домен.
 *
 * @author Artur Dombrovskii
 */
public class NotHostedDomainException extends IdentityBrokerException {

  /**
   * @param providerName имя провайдера.
   * @param email email пользователя.
   */
  public NotHostedDomainException(final String providerName, final String email) {
    super(String.format("Not hosted domain in users email %s for provider %s.", email, providerName));
    this.withMessageCode(
        String.format(
        "Ваш аккаунт не подходит для авторизации через социальную сеть (%s) с почтой (%s).",
        providerName, email)
    );
  }
}
