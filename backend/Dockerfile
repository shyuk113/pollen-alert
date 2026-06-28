FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY src/main/java/com/pollenalert/backend .
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]