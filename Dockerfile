FROM jboss/keycloak:8.0.1

COPY target/keycloak-russian-providers.jar /opt/jboss/keycloak/standalone/deployments/keycloak-russian-providers.jar
