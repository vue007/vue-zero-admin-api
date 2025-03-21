FROM openjdk:21
LABEL maintainer="Akai"

RUN mkdir -p /zero-admin/server/logs \
    /zero-admin/server/temp \
    /zero-admin/skywalking/agent

WORKDIR /zero-admin/server

ENV SERVER_PORT=8080 LANG=C.UTF-8 LC_ALL=C.UTF-8 JAVA_OPTS=""

EXPOSE ${SERVER_PORT}

COPY target/zero-admin.jar app.jar

SHELL ["/bin/bash", "-c"]

ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom -Dserver.port=${SERVER_PORT} \
           -XX:+HeapDumpOnOutOfMemoryError -XX:+UseZGC ${JAVA_OPTS} \
           -jar app.jar