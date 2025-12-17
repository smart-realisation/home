FROM gradle:8.5-jdk21 AS build
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN gradle buildFatJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*-all.jar app.jar
COPY firebase-service-account.json firebase-service-account.json
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
