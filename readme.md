# Провайдеры авторизации российских социальных сетей для Keycloak.

***Please find english version [here](readme_en.md).***

Библиотека провайдеров российских социальных сетей для [Keycloak](https://www.keycloak.org/)
+ [ВКонтакте](docs/vk.md)
+ [Однокласники](docs/ok.md)
+ [Яндекс](docs/yandex.md)
+ [Mail.Ru](docs/mailru.md)

Посмотреть на это в действии [можно здесь](https://elements.playa.ru/) - тут используется [docker образ из нашей директории на Docker Hub](https://github.com/playa-ru/keycloak-russian).

## Совместимость

Библиотека провайдеров проверялась на следующих версиях Keycloak:
+ 8.0.1
+ 4.8.3.Final
+ 4.5.0.Final
+ 4.4.0.Final
+ 3.4.3.Final

## Установка провайдеров авторизации в Keycloak

Если вы используете Docker:

- вы можете загрузить готовый Keycloak с этим модулем и парой [дополнительных тем](https://github.com/playa-ru/keycloak-playa-themes) с Docker Hub: https://github.com/playa-ru/keycloak-russian
```
docker pull playaru/keycloak-russian
```
 - или соберите проект с профилем `docker`, получится готовый образ.
```
  mvn install -Pdocker
```
Если вы не используете Docker

Можно установить библиотеку провайдеров в ваш Keycloak самостоятельно. Для этого нужно будет вручную выполнить шаги, описанные в [Dockerfile](Dockerfile), в целом [следуя инструкции](https://www.keycloak.org/docs/latest/server_development/index.html#registering-provider-implementations):

* Соберите проект из исходников с помощью Maven, или [возьмите готовый keycloak-russian-providers.jar в нашем репозитории](https://nexus.playa.ru/nexus/content/repositories/releases/ru/playa/keycloak/keycloak-russian-providers/).
* Скопируйте `keycloak-russian-providers.jar` в [директорию] `${keycloak.home.dir}/standalone/deployments`.
* Скопируйте содержимое директории `/src/main/resources/theme/base/admin/resources/partials` в `${keycloak.home.dir}/themes/base/admin/resources/partials`
* В файл `${keycloak.home.dir}/theme/base/admin/messages/admin-messages_en.properties` добавьте следующие строки:
```
ok-public-key=Application's public key
ok.public_key.tooltip=Application's public key
vk-api-version=API Version
vk.version.tooltip=VK API version
```
* В файл `${keycloak.home.dir}/theme/base/admin/messages/admin-messages_ru.properties` добавьте следующие строки:
```
ok-public-key=Публичный ключ приложения
ok.public_key.tooltip=Публичный ключ приложения
vk-api-version=API Version
vk.version.tooltip=Версия API
```
