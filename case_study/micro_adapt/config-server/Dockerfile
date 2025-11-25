FROM maven:3.9.5-eclipse-temurin-17 AS build

# Copy parent POM to root
COPY pom.xml /pom.xml

# Set up config-server directory structure
WORKDIR /config-server
COPY config-server/pom.xml ./pom.xml
COPY config-server/src ./src

# Build from service directory (Maven will find parent at ../pom.xml)
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /config-server/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
