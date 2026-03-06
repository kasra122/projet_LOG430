FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN ./gradlew build -x test

EXPOSE 8080

CMD ["java","-jar","build/libs/customer-service-0.0.1-SNAPSHOT.jar"]