FROM openjdk:17-jdk-slim

ARG CI_PROJECT_NAME
ARG CI_COMMIT_SHORT_SHA
ARG KUBE_DOMAIN
ARG PROFILE

VOLUME /tmp
ADD target/vexl-0.0.1-SNAPSHOT.jar application.jar

RUN echo "\
java \
-Dserver.use-forward-headers=true \
-Dspring.profiles.active=$PROFILE \
-Dsentry.release=$CI_COMMIT_SHORT_SHA \
-Dspringdoc.swagger-server=https://$CI_PROJECT_NAME-$CI_COMMIT_SHORT_SHA.$KUBE_DOMAIN \
-jar /application.jar \
" > /run.sh

ENTRYPOINT ["/bin/sh", "/run.sh"]
