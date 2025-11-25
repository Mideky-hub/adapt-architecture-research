FROM maven:3.9.5-eclipse-temurin-17 AS build

# Copy parent POM to root
COPY pom.xml /pom.xml

# Copy schemas to root (accessible to all services)
COPY schemas /schemas

# Set up billing-service directory structure
WORKDIR /billing-service
COPY billing-service/pom.xml ./pom.xml
COPY billing-service/src ./src

# Build from service directory (Maven will find parent at ../pom.xml)
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /billing-service/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
