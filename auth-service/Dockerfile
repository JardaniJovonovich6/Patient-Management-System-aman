    # Stage 1: Build ONLY the auth-service module
    FROM maven:3.9.9-eclipse-temurin-21 AS builder

    WORKDIR /app

    # Copy the parent pom.xml first
    COPY pom.xml .

    # Copy ONLY the auth-service module source code
    COPY auth-service ./auth-service

    # Build ONLY the auth-service module and its dependencies (-pl selects project, -am builds dependencies)
    RUN mvn clean install -pl auth-service -am -DskipTests

    # Debug: Check the target directory again after the isolated build
    RUN ls -la /app/auth-service/target/

    # Stage 2: Create the final lean image (remains the same)
    FROM openjdk:21-jdk AS runner
    ARG SERVICE_JAR_PATH
    WORKDIR /app
    COPY --from=builder ${SERVICE_JAR_PATH} app.jar
    EXPOSE 8080
    ENTRYPOINT ["java", "-jar", "app.jar"]