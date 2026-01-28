FROM eclipse-temurin:21-jre
WORKDIR /app
#
COPY build/libs/spot-payment-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=optional:classpath:/application.yml,optional:classpath:/application.properties,file:/config/application.yml"]
