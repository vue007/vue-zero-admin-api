FROM eclipse-temurin:26-jdk
LABEL maintainer="Akai"

ENV SERVER_PORT=8800 LANG=C.UTF-8 LC_ALL=C.UTF-8 JAVA_OPTS=""
WORKDIR /app
ADD ./target/zero-snailjob-server.jar ./app.jar

EXPOSE 8800 17888
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar ./app.jar"]
