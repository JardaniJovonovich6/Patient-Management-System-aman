# Stage 1: Build the entire multi-module project
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy the parent pom.xml first to leverage caching
COPY pom.xml .

# Copy the rest of the entire project source code
COPY . .

# Run the build from the root. This builds all modules.
RUN mvn clean install -DskipTests 

# ✅ ADD THESE DEBUG LINES
RUN echo "=== Checking what was built ===" && \
    ls -la /app/ && \
    echo "=== Checking auth-service target ===" && \
    ls -la /app/auth-service/target/ || echo "AUTH-SERVICE TARGET NOT FOUND!"

# Stage 2: Create the final lean image
FROM openjdk:21-jdk AS runner

ARG SERVICE_JAR_PATH

WORKDIR /app

COPY --from=builder ${SERVICE_JAR_PATH} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]







































# # Stage 1: Build the entire multi-module project
# FROM maven:3.9.9-eclipse-temurin-21 AS builder

# WORKDIR /app

# # Copy the parent pom.xml first to leverage caching
# COPY pom.xml .

# # Copy the rest of the entire project source code
# COPY . .

# # Run the build from the root. This builds all modules.
# RUN mvn clean install -DskipTests 

# RUN ls -la /app/auth-service/target/ || echo "auth-service target not found!"

# # Stage 2: Create the final lean image
# FROM openjdk:21-jdk AS runner

# # Define an argument to tell this stage which JAR file to run
# ARG SERVICE_JAR_PATH

# WORKDIR /app

# # Copy the specific JAR file from the builder stage
# COPY --from=builder ${SERVICE_JAR_PATH} app.jar

# # Expose a default port (this will be overridden by docker-compose)
# EXPOSE 8080

# ENTRYPOINT ["java", "-jar", "app.jar"]