FROM registry.access.redhat.com/ubi9 AS ubi-micro-build
RUN mkdir -p /mnt/rootfs
RUN dnf install --installroot /mnt/rootfs unzip --releasever 9 --setopt install_weak_deps=false --nodocs -y; dnf --installroot /mnt/rootfs clean all

FROM quay.io/keycloak/keycloak:21.0.1 as builder
COPY --from=ubi-micro-build /mnt/rootfs /

ARG JAR_FILE
ARG DB_PROVIDER

ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true
ENV KC_FEATURES=token-exchange
ENV KC_DB=$DB_PROVIDER
ENV KC_HTTP_RELATIVE_PATH=/auth

ENV JBOSS_HOME /opt/keycloak
ENV THEMES_HOME $JBOSS_HOME/themes
ENV THEMES_BASE_TMP /tmp/keycloak-base-themes
ENV PROVIDERS_TMP /tmp/keycloak-providers
ENV LIBS_TMP /tmp/keycloak-libs

ENV KEYCLOAK_VERSION 21.0.1
ENV KEYCLOAK_ADMIN_THEME 1.0.7

RUN mkdir -p $PROVIDERS_TMP
RUN mkdir -p $THEMES_BASE_TMP

USER root

RUN echo "DataBase is $DB_PROVIDER"
RUN echo "JAR_FILE is $JAR_FILE"

RUN unzip /opt/keycloak/lib/lib/main/org.keycloak.keycloak-themes-$KEYCLOAK_VERSION.jar -d $THEMES_BASE_TMP
RUN mv $THEMES_BASE_TMP/theme/* $THEMES_HOME

COPY target/$JAR_FILE $PROVIDERS_TMP/keycloak-russian-providers.jar

ADD https://repo1.maven.org/maven2/com/jayway/jsonpath/json-path/2.7.0/json-path-2.7.0.jar $JBOSS_HOME/providers
ADD https://repo1.maven.org/maven2/net/minidev/json-smart/2.4.7/json-smart-2.4.7.jar $JBOSS_HOME/providers
ADD https://nexus.playa.ru/nexus/content/repositories/releases/org/keycloak/keycloak-admin-ui/$KEYCLOAK_ADMIN_THEME/keycloak-admin-ui-$KEYCLOAK_ADMIN_THEME.jar $PROVIDERS_TMP

RUN cp $PROVIDERS_TMP/keycloak-admin-ui-$KEYCLOAK_ADMIN_THEME.jar $JBOSS_HOME/lib/lib/main/org.keycloak.keycloak-admin-ui-$KEYCLOAK_VERSION.jar

RUN cp $PROVIDERS_TMP/keycloak-russian-providers.jar $JBOSS_HOME/providers
RUN unzip $PROVIDERS_TMP/keycloak-russian-providers.jar -d $PROVIDERS_TMP
RUN cat $PROVIDERS_TMP/theme/base/login/messages/messages_en.custom >> $THEMES_HOME/base/login/messages/messages_en.properties
RUN cat $PROVIDERS_TMP/theme/base/login/messages/messages_ru.custom >> $THEMES_HOME/base/login/messages/messages_ru.properties
RUN cat $PROVIDERS_TMP/theme/base/admin/messages/admin-messages_en.custom >> $THEMES_HOME/base/admin/messages/admin-messages_en.properties
RUN cat $PROVIDERS_TMP/theme/base/admin/messages/admin-messages_ru.custom >> $THEMES_HOME/base/admin/messages/admin-messages_ru.properties

RUN chmod -R a+r $JBOSS_HOME

RUN rm -rf $PROVIDERS_TMP
RUN rm -rf $THEMES_BASE_TMP

USER 1000

RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:21.0.1
COPY --from=builder /opt/keycloak/ /opt/keycloak/
WORKDIR /opt/keycloak

# for demonstration purposes only, please make sure to use proper certificates in production instead
RUN keytool -genkeypair -storepass password -storetype PKCS12 -keyalg RSA -keysize 2048 -dname "CN=server" -alias server -ext "SAN:c=DNS:localhost,IP:127.0.0.1" -keystore conf/server.keystore
# change these values to point to a running postgres instance

ENV KC_DB_URL=<DBURL>
ENV KC_DB_USERNAME=<DBUSERNAME>
ENV KC_DB_PASSWORD=<DBPASSWORD>
ENV KC_HTTP_ENABLED=<HTTPENABLED>
ENV KC_HOSTNAME_STRICT=<HOSTNAMESTRICT>

ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start"]