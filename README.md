# **Project Update: API Gateway & JWT Authentication Service**

This document details a major architectural update to the Patient Management System. This phase moves the project from a simple internal-system to a secure, robust, and scalable application by introducing a dedicated **API Gateway** and a **Stateless Authentication Service**.

This update was a multi-day debugging marathon that involved solving complex, real-world issues related to build caches, Spring Security configuration, and inter-service communication.

## **1\. New Services Added**

### **api-gateway**

* **Purpose:** Acts as the single "front door" for the entire application. All client (frontend) requests will now go to http://localhost:4000.  
* **Technology:** Spring Cloud Gateway.  
* **Key Changes:**  
  * **application.yml:** Configured with specific routes to forward traffic to the correct microservice.  
  * **Routing:**  
    * http://localhost:4000/patients/\*\* is routed to the patient-service.  
    * http://localhost:4000/api-docs/patients is routed and rewritten to the patient-service's OpenAPI docs.  
    * *Future:* http://localhost:4000/auth/\*\* will be routed to the auth-service.  
  * **Service Discovery:** Uses direct http://container-name:port addressing (e.g., http://patient-service:8080) to communicate within the Docker network, avoiding the need for a separate discovery client.

### **auth-service**

* **Purpose:** A dedicated microservice responsible for all user management, authentication, and JWT generation/validation.  
* **Technology:** Spring Boot, Spring Security 6, JWT (jjwt), JPA.  
* **Key Features:**  
  * **POST /login:** Authenticates a user against the database and returns a signed JWT.  
  * **GET /validate:** A secure endpoint (now working\!) that validates an incoming JWT. This will be used by the API Gateway.  
  * **SecurityConfig.java:** A complex but correct configuration that establishes a stateless (SessionCreationPolicy.STATELESS) security model.  
  * **UserService.java:** Implements UserDetailsService to act as the official "guest list" for Spring Security.  
  * **DataSeeder.java:** A CommandLineRunner that creates a dummy, password-encoded user on startup for reliable testing.

### **auth-service-db**

* **Purpose:** A dedicated PostgreSQL database instance for the auth-service.  
* **Reasoning:** Follows the "one database per service" microservice pattern. This isolates user and credential data from patient and billing data, which is a critical security and design principle.

## **2\. Key Changes to Existing Files**

### **docker-compose.yaml**

This file was heavily modified to orchestrate the entire, complex system.

* **Added auth-service-db:** A new PostgreSQL container with its own unique port (5434) and a new named volume (auth-service-db-data).  
* **Added auth-service:** The new service, configured to connect to auth-service-db.  
* **Injected JWT\_SECRET:** The JWT secret key is now provided as an **environment variable** (jwt.secret=...) to the auth-service, which is a security best practice (no hard-coded secrets\!).  
* **Added api-gateway:** The new "front door" service, configured with port 4000 and depends\_on to ensure it starts last.  
* **Updated db-init/init.sql:** Added CREATE DATABASE auth\_db; so the new database is created on first launch.

## **3\. New Skills & Concepts I Learned (The Debugging Marathon)**

This was not a simple tutorial. I had to diagnose and fix multiple, advanced, real-world problems.

* **Debugging "Phantom Builds" (The Cache Hell):** My \#1 problem. I would change code, but the running application would be old.  
  * **The Bug:** Docker's build cache and VS Code's Java Server cache were serving stale files.  
  * **The Skill:** I learned to defeat this by:  
    1. **Nuking the Docker Cache:** docker system prune \-a  
    2. **The "POM Comment" Trick:** Modifying the root pom.xml to invalidate the cache.  
    3. **Nuking the IDE Cache:** Using Java: Clean Java Language Server Workspace in VS Code.  
    4. **Bypassing Stale JARs:** Using ./mvnw.cmd spring-boot:run to compile and run in one step, ensuring the latest code is always used.  
* **Advanced Spring Security 6:** I didn't just turn it on; I configured it from scratch.  
  * **The Bug:** My POST /login worked, but /validate gave a 403 Forbidden with no logs.  
  * **The "Two Bouncers" Problem:** I learned that Spring's default AuthenticationManager was not using my UserService.  
  * **The Skill:** I fixed this by implementing the full, professional Spring Security chain:  
    1. My UserService now **implements UserDetailsService**.  
    2. I created a CustomUserDetails wrapper class.  
    3. I created an AuthenticationProvider bean in SecurityConfig to "glue" my UserService and PasswordEncoder together.  
    4. I created a JwtAuthFilter to act as the "keycard reader" for my JWTs.  
    5. I configured the SecurityFilterChain to be **STATELESS** and use my new filter, which is the correct pattern for JWT and microservices.  
* **Spring Boot Testing vs. Running:**  
  * **The Bug:** mvn clean install would BUILD FAILURE because my *tests* were running "blind" without any properties.  
  * **The Skill:** I learned to create a separate **src/test/resources/application.properties** file to provide dummy H2 and JWT values, allowing the build to pass.  
* **Spring Profiles (The H2 Workflow):**  
  * The Skill: I learned to create an application-h2.properties file for fast local testing. I can now run any service independently with the "One True Command":  
    ./mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=h2"  
* **The "Evil" Import Bug (The Silent Failure):**  
  * **The Bug:** My POST /login was completely silent (no logs, no 401, nothing).  
  * **The Skill:** I diagnosed this by seeing that a GET request gave a 405 (proving the controller was alive). The bug was a single wrong import: io.swagger.v3.oas.annotations.parameters.RequestBody instead of the correct org.springframework.web.bind.annotation.RequestBody. This taught me to **always check my imports** when debugging silent failures.