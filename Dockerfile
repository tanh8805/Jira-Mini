FROM gradle:8.5-jdk17 as builder

WORKDIR /app

COPY . .

RUN ./gradlew build -x test

FROM eclipse-temurin:17-jre-alpine as runner

WORKDIR /app

COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]