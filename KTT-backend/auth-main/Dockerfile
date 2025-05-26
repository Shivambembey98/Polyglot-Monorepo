# Stage 1: Build auth-main + common
FROM maven:3.8.7-eclipse-temurin-17 AS build
WORKDIR /app

# Copy parent pom
COPY ./pom.xml ./pom.xml

# Copy module and shared module
COPY ./auth-main ./auth-main
COPY ./shared ./shared

# Build only auth-main module
RUN mvn -pl auth-main -am clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy built JAR
COPY --from=build /app/auth-main/target/auth-main-*.jar app.jar

# Copy .env if exists
COPY ./auth-main/.env ./

# Expose port
EXPOSE 8080

# Use PORT env var or default 8080
ENV PORT=8080

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=$PORT"]
