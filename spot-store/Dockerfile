FROM eclipse-temurin:21-jre
WORKDIR /app

COPY build/libs/spot-store-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_CONFIG_ADDITIONAL_LOCATION=file:/config/

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
