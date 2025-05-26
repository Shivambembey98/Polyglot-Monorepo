# Stage 1: Build booking + common
FROM maven:3.8.7-eclipse-temurin-17 AS build
WORKDIR /app

COPY ./pom.xml ./pom.xml
COPY ./booking ./booking
COPY ./shared ./shared

RUN mvn -pl booking -am clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/booking/target/booking-*.jar app.jar

COPY ./booking/.env ./

EXPOSE 8082
ENV PORT=8082

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=$PORT"]
