FROM openjdk:21
LABEL maintainer="Akai"

RUN mkdir -p /zero-admin/server/logs \
    /zero-admin/server/temp \

ENV SERVER_PORT=8080 LANG=C.UTF-8 LC_ALL=C.UTF-8 JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE=dev
ADD ./target/zero-admin-web.jar ./app.jar

EXPOSE 8080
CMD ["java", "-jar", "./app.jar"]