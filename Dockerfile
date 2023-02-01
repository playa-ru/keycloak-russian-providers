FROM quay.io/keycloak/keycloak:20.0.3

ENV KEYCLOAK_HOME /opt/keycloak

COPY target/keycloak-russian-providers.jar $KEYCLOAK_HOME/providers/keycloak-russian-providers.jar
