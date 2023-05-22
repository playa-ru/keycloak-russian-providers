FROM registry.access.redhat.com/ubi9 AS ubi-micro-build
RUN mkdir -p /mnt/rootfs
RUN dnf install --installroot /mnt/rootfs unzip curl --releasever 9 --setopt install_weak_deps=false --nodocs -y; dnf --installroot /mnt/rootfs clean all

FROM quay.io/keycloak/keycloak:21.1.1 as builder
COPY --from=ubi-micro-build /mnt/rootfs /

ARG JAR_FILE
ARG PLAYA_RU_GITHUB_TOKEN
ENV PLAYA_RU_GITHUB_TOKEN ${PLAYA_RU_GITHUB_TOKEN}

ENV JBOSS_HOME /opt/keycloak
ENV THEMES_HOME $JBOSS_HOME/themes
ENV THEMES_BASE_TMP /tmp/keycloak-base-themes
ENV PROVIDERS_TMP /tmp/keycloak-providers
ENV LIBS_TMP /tmp/keycloak-libs

ENV KEYCLOAK_VERSION 21.1.1
ENV KEYCLOAK_ADMIN_THEME 21.1.1.rsp-12

RUN mkdir -p $PROVIDERS_TMP
RUN mkdir -p $THEMES_BASE_TMP

USER root

RUN echo "JAR_FILE is $JAR_FILE"

RUN unzip /opt/keycloak/lib/lib/main/org.keycloak.keycloak-themes-$KEYCLOAK_VERSION.jar -d $THEMES_BASE_TMP
RUN mv $THEMES_BASE_TMP/theme/* $THEMES_HOME

COPY target/$JAR_FILE $PROVIDERS_TMP/keycloak-russian-providers.jar

ADD https://repo1.maven.org/maven2/com/jayway/jsonpath/json-path/2.7.0/json-path-2.7.0.jar $JBOSS_HOME/providers
ADD https://repo1.maven.org/maven2/net/minidev/json-smart/2.4.7/json-smart-2.4.7.jar $JBOSS_HOME/providers
RUN curl -X GET --location "https://maven.pkg.github.com/playa-ru/keycloak-ui/org/keycloak/keycloak-admin-ui/$KEYCLOAK_ADMIN_THEME/keycloak-admin-ui-$KEYCLOAK_ADMIN_THEME.jar" -H "Authorization: Bearer $PLAYA_RU_GITHUB_TOKEN" -o $PROVIDERS_TMP/keycloak-admin-ui-$KEYCLOAK_ADMIN_THEME.jar

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

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]