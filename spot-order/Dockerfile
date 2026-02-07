FROM eclipse-temurin:21-jre
WORKDIR /app

COPY build/libs/spot-order-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_CONFIG_ADDITIONAL_LOCATION=file:/config/

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
