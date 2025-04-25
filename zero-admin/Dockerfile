FROM openjdk:21-ea-jdk
LABEL maintainer="Akai"

ENV SERVER_PORT=8080 LANG=C.UTF-8 LC_ALL=C.UTF-8 JAVA_OPTS=""

ENV SPRING_PROFILES_ACTIVE=prod
ADD ./zero-admin/target/zero-admin-web.jar ./app.jar

EXPOSE 8080
CMD ["java", "-jar", "./app.jar"]