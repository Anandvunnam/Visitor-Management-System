# ----------- Stage 1: Build JAR using Maven -----------
FROM maven:3.9-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies early (cached)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy all source code
COPY src ./src
COPY wait-for-it.sh /app/
COPY .env /app/
# Package the Spring Boot app (produces a fat jar)
RUN mvn clean package -DskipTests

# ----------- Stage 2: Run the JAR -----------
FROM amazoncorretto:17

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar
COPY --from=build /app/wait-for-it.sh /app/wait-for-it.sh
COPY --from=build /app/.env /app/.env
RUN chmod +x /app/wait-for-it.sh

# Expose port (optional)
EXPOSE 8080

# Run the application
ENTRYPOINT ["/app/wait-for-it.sh", "mysql:3306", "--", "/app/wait-for-it.sh", "redis:6379", "--","java","-jar","app.jar"]
