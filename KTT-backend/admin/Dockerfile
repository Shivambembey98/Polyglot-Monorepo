# Stage 1: Build admin + common
FROM maven:3.8.7-eclipse-temurin-17 AS build
WORKDIR /app

COPY ./pom.xml ./pom.xml
COPY ./admin ./admin
COPY ./shared ./shared

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/admin/target/admin-*.jar app.jar

COPY ./admin/.env ./

EXPOSE 9002
ENV PORT=9002

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=$PORT"]
