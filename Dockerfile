FROM jboss/keycloak:4.4.0.Final

ENV JBOSS_HOME /opt/jboss/keycloak
ENV THEMES_HOME $JBOSS_HOME/themes/base/admin

COPY /target/classes/theme/base/admin/resources/partials $THEMES_HOME/resources/partials
COPY /target/classes/theme/base/admin/messages/ $THEMES_HOME/messages
COPY target/keycloak-russian.jar $JBOSS_HOME/standalone/deployments/keycloak-russian.jar
