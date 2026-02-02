FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app

COPY /build/libs/service.jar build/

WORKDIR /app/build
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "service.jar"]