FROM maven:3.9-eclipse-temurin-17 AS build

# Copy parent POM to root
COPY pom.xml /pom.xml

# Copy schemas to root (accessible to all services)
COPY schemas /schemas

# Set up order-service directory structure
WORKDIR /order-service
COPY order-service/pom.xml ./pom.xml
COPY order-service/src ./src

# Build from service directory (Maven will find parent at ../pom.xml)
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /order-service/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
