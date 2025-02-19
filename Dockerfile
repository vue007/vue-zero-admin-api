# 使用 OpenJDK 17 作为基础镜像
FROM openjdk:17

# 定义构建参数，用于接收 JAR 文件名
ARG JAR_FILE=target/*.jar

# 设置工作目录
WORKDIR /app

# 将 JAR 文件复制到容器中
COPY ${JAR_FILE} app.jar

EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]