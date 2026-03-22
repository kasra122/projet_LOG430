FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# ONLY copy customer-service
COPY services/customer-service .

RUN chmod +x gradlew
RUN sed -i 's/\r$//' gradlew

RUN ./gradlew clean build -x test

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
