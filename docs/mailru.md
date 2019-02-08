# Mail.Ru.

1. Из списка провайдеров выбрать [Mail.Ru](https://mail.ru). 
![Выбор провайдера](screenshots/mailru_provider_1.png)
2. Зарегистрировать сайт в [Mail.Ru](https://oauth.mail.ru/app).
![Регистрация сайта](screenshots/mailru_provider_2.png)
+ В открывшемся окне необходимо заполнить поле *Название проекта*.
+ Значение поля *Все redirect_uri* заполнить значением *Redirect URI* из админки Keycloak. 
3. Нажимаем кнопку "Подключить сайт", после чего откроется окно с данными приложения.
![Окно с данными приложения](screenshots/mailru_provider_3.png)
4. Копируем значение *ID Приложения / Client ID* в поле *Client ID* в админке *Keycloak*.
5. Копируем значение *Секрет / Client Secret* в поле *Client Secret* в админке *Keycloak*.
6. Нажимаем кнопку *Save* в админке *Keycloak*.
