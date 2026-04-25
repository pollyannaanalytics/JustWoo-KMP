FROM amazoncorretto:21-alpine-jdk

WORKDIR /app

COPY build/libs/backend-all.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "app.jar"]