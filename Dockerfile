# 使用 OpenJDK 21 作为基础镜像
FROM openjdk:21

# 添加镜像信息
LABEL maintainer="akai007" \
      description="Zero Admin Api"

# 定义构建参数，用于接收 JAR 文件名
ARG JAR_FILE=target/*.jar
ARG WORK_PATH="/app"

# 设置时区为上海
ENV TZ=Asia/Shanghai

# 设置工作目录
WORKDIR ${WORK_PATH}

# 将 JAR 文件复制到容器中
COPY ${JAR_FILE} app.jar

EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", \
    "-Xms512m", \
    "-Xmx512m", \
    "-XX:+UseG1GC", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-jar", \
    "app.jar"]