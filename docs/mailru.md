# Mail.Ru.

1. Из списка провайдеров выбрать [Mail.Ru](https://mail.ru). 
![Выбор провайдера](screenshots/mailru_provider_1.png)
2. Создать приложение в [MailRu](https://api.mail.ru/sites/my/add).
3. Принимаем *Правила использования*. 
![Правила использования](screenshots/mailru_provider_2.png)
4. Заполняем *Информация о сайте*.
![Информация о сайте](screenshots/mailru_provider_3.png)
5. Этап *Настрока сайта* пропускаем. 
![Информация о сайте](screenshots/mailru_provider_4.png)
6. Заходим в редактирование настроек, только что созданного приложения. 
![Информация о сайте](screenshots/mailru_provider_5.png)
+ Значение поля *Адрес страницы receiver.html* заполнить значением *Redirect URI* из админки Keycloak.
7. Копируем значение *ID* в поле *Client ID* в админке *Keycloak*.
8. Копируем значение *Секретный ключ приложения* в поле *Client Secret* в админке *Keycloak*.
9. Нажимаем кнопку *Save* в админке *Keycloak*.
