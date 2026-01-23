FROM eclipse-temurin:21-jre
WORKDIR /app

COPY build/libs/spot-gateway.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=file:/config/application.yml"]
