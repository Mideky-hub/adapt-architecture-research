FROM maven:3.9.5-eclipse-temurin-17 AS build

# Copy parent POM to root
COPY pom.xml /pom.xml

# Copy schemas to root (accessible to all services)
COPY schemas /schemas

# Set up inventory-service directory structure
WORKDIR /inventory-service
COPY inventory-service/pom.xml ./pom.xml
COPY inventory-service/src ./src

# Build from service directory (Maven will find parent at ../pom.xml)
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /inventory-service/target/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
