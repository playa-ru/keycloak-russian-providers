FROM bellsoft/liberica-openjdk-centos:17 AS ubi-micro-install

ARG JAR_FILE
ARG PLAYA_RU_GITHUB_TOKEN

ARG TMP_DIST=/tmp/keycloak

ENV KEYCLOAK_VERSION 22.0.3
ENV PLAYA_THEMES_VERSION 1.0.22
ENV JSON_PATH_VERSION 2.7.0
ENV JSON_SMART_VERSION 2.4.7

ENV MAVEN_CENTRAL_URL https://repo1.maven.org/maven2
ENV NEXUS_URL https://nexus.playa.ru/nexus/content/repositories/releases

ARG KEYCLOAK_DIST=https://github.com/keycloak/keycloak/releases/download/$KEYCLOAK_VERSION/keycloak-$KEYCLOAK_VERSION.tar.gz
ARG JSON_PATH_DIST=https://repo1.maven.org/maven2/com/jayway/jsonpath/json-path/$JSON_PATH_VERSION/json-path-$JSON_PATH_VERSION.jar
ARG JSON_SMART_DIST=https://repo1.maven.org/maven2/net/minidev/json-smart/$JSON_SMART_VERSION/json-smart-$JSON_SMART_VERSION.jar

RUN yum install -y curl tar gzip unzip

ADD $KEYCLOAK_DIST $TMP_DIST/
ADD $JSON_PATH_DIST $TMP_DIST/
ADD $JSON_SMART_DIST $TMP_DIST/
COPY target/$JAR_FILE $TMP_DIST/keycloak-russian-providers-$KEYCLOAK_VERSION.jar

RUN cd /tmp/keycloak && tar -xvf /tmp/keycloak/keycloak-*.tar.gz && rm /tmp/keycloak/keycloak-*.tar.gz

RUN mv $TMP_DIST/keycloak-russian-providers-$KEYCLOAK_VERSION.jar $TMP_DIST/keycloak-$KEYCLOAK_VERSION/providers/keycloak-russian-providers-$KEYCLOAK_VERSION.jar
RUN mv $TMP_DIST/json-path-$JSON_PATH_VERSION.jar $TMP_DIST/keycloak-$KEYCLOAK_VERSION/providers/json-path-$JSON_PATH_VERSION.jar
RUN mv $TMP_DIST/json-smart-$JSON_SMART_VERSION.jar $TMP_DIST/keycloak-$KEYCLOAK_VERSION/providers/json-smart-$JSON_SMART_VERSION.jar

RUN mkdir -p /opt/keycloak && mv /tmp/keycloak/keycloak-$KEYCLOAK_VERSION/* /opt/keycloak && mkdir -p /opt/keycloak/data

RUN chmod -R g+rwX /opt/keycloak

FROM bellsoft/liberica-openjdk-centos:17 AS ubi-micro-chown
ENV LANG en_US.UTF-8

COPY --from=ubi-micro-install --chown=1000:0 /opt/keycloak /opt/keycloak

RUN echo "keycloak:x:0:root" >> /etc/group && \
    echo "keycloak:x:1000:0:keycloak user:/opt/keycloak:/sbin/nologin" >> /etc/passwd

USER 1000

EXPOSE 8080
EXPOSE 8443

ENTRYPOINT [ "/opt/keycloak/bin/kc.sh" ]
