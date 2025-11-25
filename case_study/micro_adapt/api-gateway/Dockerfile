FROM maven:3.9.5-eclipse-temurin-17 AS build

# Copy parent POM to root
COPY pom.xml /pom.xml

# Set up api-gateway directory structure
WORKDIR /api-gateway
COPY api-gateway/pom.xml ./pom.xml
COPY api-gateway/src ./src

# Build from service directory (Maven will find parent at ../pom.xml)
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /api-gateway/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
