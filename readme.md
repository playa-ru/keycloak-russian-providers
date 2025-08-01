# Провайдеры авторизации российских социальных сетей для Keycloak.

***Please find english version [here](readme_en.md).***

Библиотека провайдеров российских социальных сетей для [Keycloak](https://www.keycloak.org/)
+ [ВКонтакте](docs/vk.md)
+ [Однокласники](docs/ok.md)
+ [Яндекс](docs/yandex.md)
+ [Mail.Ru](docs/mailru.md)

Посмотреть на это в действии [можно здесь](https://my.yachtex.ru/) - тут используется [docker образ из нашей директории на Docker Hub](https://github.com/playa-ru/keycloak-russian).

## Мы переехали!

Теперь проект живёт в [GitVerse](https://gitverse.ru/playa.ru/keycloak-russian-providers)
Подпишитесь там, чтобы быть в курсе обновлений.

Репозиторий на [GitHub](https://github.com/playa-ru/keycloak-russian-providers) остаётся зеркалом и содержит
актуальные исходные коды, но все изменения вносятся через новый репозиторий.

## Совместимость

Библиотека провайдеров проверялась на следующих версиях Keycloak:
+ 26.0.0
+ 25.0.2
+ 24.0.1
+ 23.0.6
+ 22.0.3
+ 21.1.1
+ 21.0.1
+ 17.0.0
+ 16.1.1
+ 15.0.2
+ 13.0.0
+ 12.0.1
+ 11.0.3
+ 10.0.0
+ 8.0.1
+ 4.8.3.Final
+ 4.5.0.Final
+ 4.4.0.Final
+ 3.4.3.Final

**Таблица поддерживаемых версий:**

| Версия библиотеки | Версия Keycloak |                   Репозиторий                    |
|:-----------------:|:---------------:|:------------------------------------------------:|
|    26.0.0.rsp     |     26.0.0      |    [Maven Central](https://mvnrepository.com)    |
|   25.0.2.rsp-2    |     25.0.2      |    [Maven Central](https://mvnrepository.com)    |
|   24.0.1.rsp-3    |     24.0.1      |    [Maven Central](https://mvnrepository.com)    |
|   23.0.6.rsp-3    |     23.0.6      |    [Maven Central](https://mvnrepository.com)    |
|    22.0.3.rsp     |     22.0.3      |    [Maven Central](https://mvnrepository.com)    |
|    21.1.1.rsp     |     21.1.1      |    [Maven Central](https://mvnrepository.com)    |
|      1.0.46       |     21.0.1      |    [Maven Central](https://mvnrepository.com)    |
|      1.0.43       |     17.0.0      |    [Maven Central](https://mvnrepository.com)    |
|      1.0.42       |     16.1.1      |    [Maven Central](https://mvnrepository.com)    |
|      1.0.38       |     15.0.2      |    [Maven Central](https://mvnrepository.com)    |
|      1.0.37       |     13.0.0      |    [Maven Central](https://mvnrepository.com)    |
|      1.0.32       |     12.0.4      |    [Maven Central](https://mvnrepository.com)    |
|      1.0.28       |     12.0.0      | [Playa Repository](https://nexus.playa.ru/nexus) |
|      1.0.26       |     12.0.1      | [Playa Repository](https://nexus.playa.ru/nexus) |
|      1.0.25       |     11.0.3      | [Playa Repository](https://nexus.playa.ru/nexus) | 
|      1.0.21       |     10.0.0      | [Playa Repository](https://nexus.playa.ru/nexus) |
|      1.0.17       |      8.0.1      | [Playa Repository](https://nexus.playa.ru/nexus) |
|      1.0.16       |      6.0.1      | [Playa Repository](https://nexus.playa.ru/nexus) |
|      1.0.15       |   4.8.3.Final   | [Playa Repository](https://nexus.playa.ru/nexus) |
|       1.0.1       |   4.5.0.Final   | [Playa Repository](https://nexus.playa.ru/nexus) |

## Установка провайдеров авторизации в Keycloak

Если вы используете Docker:

- вы можете загрузить готовый Keycloak с этим модулем и парой [дополнительных тем](https://github.com/playa-ru/keycloak-playa-themes) с Docker Hub: https://github.com/playa-ru/keycloak-russian
```
docker pull playaru/keycloak-russian
```
 - или соберите проект с профилем `docker`, получится готовый образ. Например, сборка образа Keycloak:
```
  mvn install -Pdocker
```

Если вы не используете Docker 

Можно установить библиотеку провайдеров в ваш Keycloak самостоятельно. 

## Keycloak <= 16.1.1

Для этого нужно будет вручную выполнить шаги, описанные в [Dockerfile](Dockerfile), в целом [следуя инструкции](https://www.keycloak.org/docs/latest/server_development/index.html#registering-provider-implementations):

1. Соберите проект из исходников с помощью Maven, или [возьмите готовый keycloak-russian-providers.jar в нашем репозитории](https://s01.oss.sonatype.org/content/groups/public/ru/playa/keycloak/keycloak-russian-providers/). 
2. Скопируйте `keycloak-russian-providers.jar` в [директорию] `${keycloak.home.dir}/standalone/deployments`.
3. Скопируйте содержимое директории `/src/main/resources/themes/base/admin/resources/partials` в `${keycloak.home.dir}/themes/base/admin/resources/partials`
4. Добавьте переводы необходимые для темы, для этого необходимо дополнить файлы:
   `${keycloak.home.dir}/themes/base/admin/messages/admin-messages_en.custom` (
   из файла `src/main/resources/theme/base/admin/messages/admin-messages_en.custom`)
   `${keycloak.home.dir}/themes/base/admin/messages/admin-messages_ru.custom` (
   из файла `src/main/resources/theme/base/admin/messages/admin-messages_ru.custom`)
   `${keycloak.home.dir}/themes/base/login/messages/messages_en.properties` (
   из файла `src/main/resources/theme/base/login/messages/messages_en.custom`)
   `${keycloak.home.dir}/themes/base/login/messages/messages_ru.properties` (
   из файла `src/main/resources/theme/base/login/messages/messages_ru.custom`)

## Keycloak 17.0.0 - 20.0.5

Для установки модуля авторизации через ЕСИА необходимо выполнить следующие шаги:

1. Соберите проект из исходников с помощью Maven, или [возьмите готовый keycloak-russian-providers.jar в нашем репозитории](https://s01.oss.sonatype.org/content/groups/public/ru/playa/keycloak/keycloak-russian-providers/).
2. Скопируйте `keycloak-russian-providers.jar` файл в директорию `/providers`
3. Скопируйте `json-path-2.7.0.jar`, `json-smart-2.4.7.jar` файлы в директорию `/providers`
4. Разархивируйте стандартные темы в папку `/themes` (стандартные темы находятся по
   пути `/lib/lib/main/org.keycloak.keycloak-themes-${keycloak-version}`, где `keycloak-version` версия Keycloak)
5. Скопируйте содержимое директории `/src/main/resources/themes/base/admin/resources/partials` в `${keycloak.home.dir}/themes/base/admin/resources/partials`   
6. Добавьте переводы необходимые для темы, для этого необходимо дополнить файлы:
   `${keycloak.home.dir}/themes/base/admin/messages/admin-messages_en.custom` (
   из файла `src/main/resources/theme/base/admin/messages/admin-messages_en.custom`)
   `${keycloak.home.dir}/themes/base/admin/messages/admin-messages_ru.custom` (
   из файла `src/main/resources/theme/base/admin/messages/admin-messages_ru.custom`)
   `${keycloak.home.dir}/themes/base/login/messages/messages_en.properties` (
   из файла `src/main/resources/theme/base/login/messages/messages_en.custom`)
   `${keycloak.home.dir}/themes/base/login/messages/messages_ru.properties` (
   из файла `src/main/resources/theme/base/login/messages/messages_ru.custom`)
7. Выполнить установку новых модулей для этого необходимо выполнить команду `/bin/kc.sh build`

## Keycloak >= 21.1.1

1. Соберите проект из исходников с помощью Maven, или [возьмите готовый keycloak-russian-providers.jar в нашем репозитории](https://s01.oss.sonatype.org/content/groups/public/ru/playa/keycloak/keycloak-russian-providers/).
2. Скопируйте `keycloak-russian-providers.jar` файл в директорию `/providers`
3. Скопируйте `json-path-2.7.0.jar`, `json-smart-2.4.7.jar` файлы в директорию `/providers`
4. Скачайте обновленную версию библиотеки `keycloak-admin-ui` с [github](https://github.com/playa-ru/keycloak-ui/packages/1871123). 
   Шаблон формирования версии библиотеки `${keycloak-version}.rsp`
5. Скопируйте `keycloak-admin-ui` в `/lib/lib/main/org.keycloak.keycloak-admin-ui-${keycloak-version}.jar`
6. Выполнить установку новых модулей для этого необходимо выполнить команду `/bin/kc.sh build`

## Keycloak >= 23.0.0

1. Соберите проект из исходников с помощью Maven, или [возьмите готовый keycloak-russian-providers.jar в нашем репозитории](https://s01.oss.sonatype.org/content/groups/public/ru/playa/keycloak/keycloak-russian-providers/).
2. Скопируйте `keycloak-russian-providers.jar` файл в директорию `/providers`
3. Скопируйте `json-path-2.7.0.jar`, `json-smart-2.4.7.jar` файлы в директорию `/providers`
4. Выполнить установку новых модулей для этого необходимо выполнить команду `/bin/kc.sh build`
