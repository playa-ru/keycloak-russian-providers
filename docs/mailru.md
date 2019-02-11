# Mail.Ru.

**Регистрация сайта в [Mail.Ru](https://oauth.mail.ru/app)**
+ Заполняем поле *Название*.
+ Заполняем поле *Все redirect_uri* - https://${keycloak-host}/auth/realms/${realm-name}/broker/mailru/endpoint, где
 _keycloak-host_ - домен на котором размещен _Keycloak_,
 _realm-name_ - название _Realm_ в _Keycloak_.
![Доверенный redirect URI](screenshots/mailru_provider_1.png)
+ Нажимаем кнопку *Подключить сайт*.
![Приложение в MailRu](screenshots/mailru_provider_2.png)

**Создание провайдера Keycloak**
1. Из списка провайдеров выбрать [Mail.Ru](https://mail.ru). .
![Выбор провайдера](screenshots/mailru_provider_3.png)
2. Копируем значение *ID Приложения / Client ID* в поле *Client ID* в админке *Keycloak*.
3. Копируем значение *Секрет / Client Secret* в поле *Client Secret* в админке *Keycloak*.
4. Нажимаем кнопку *Save* в админке *Keycloak*.
![Создание провайдера Keycloak](screenshots/mailru_provider_4.png)
