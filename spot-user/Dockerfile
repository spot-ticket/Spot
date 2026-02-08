FROM eclipse-temurin:21-jre
WORKDIR /app

COPY build/libs/spot-user-0.0.1-SNAPSHOT.jar app.jar

# /config 디렉토리의 설정 파일을 추가로 읽음
ENV SPRING_CONFIG_ADDITIONAL_LOCATION=file:/config/

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
