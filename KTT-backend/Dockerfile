# Stage 1: Build all modules
FROM maven:3.8.7-eclipse-temurin-17 AS build
WORKDIR /app

# Safer explicit copy
COPY ./pom.xml ./pom.xml
COPY ./auth-main ./auth-main
COPY ./booking ./booking
COPY ./admin ./admin
COPY ./shared ./shared


RUN mvn clean package -DskipTests

# Stage 2: Run selected module
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# ARG for module selection (default is auth-main)
ARG MODULE=auth-main

# Copy selected module jar
COPY --from=build /app/${MODULE}/target/*.jar app.jar

# Copy .env file (optional)
COPY .env ./

# Default port (can override at runtime)
ENV PORT=8080

# Expose all possible ports
EXPOSE 8080 8082 9002

# Use dynamically set PORT during runtime
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=$PORT"]
