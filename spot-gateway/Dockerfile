FROM eclipse-temurin:21-jre
WORKDIR /app

COPY build/libs/*.jar app.jar

ENV SPRING_CONFIG_ADDITIONAL_LOCATION=file:/config/

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
