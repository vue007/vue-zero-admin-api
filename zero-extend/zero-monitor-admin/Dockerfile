FROM eclipse-temurin:26-jdk
LABEL maintainer="Akai"

ENV SERVER_PORT=9090 LANG=C.UTF-8 LC_ALL=C.UTF-8 JAVA_OPTS=""
WORKDIR /app
ADD ./target/zero-monitor-admin.jar ./app.jar

EXPOSE 9090
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar ./app.jar"]
