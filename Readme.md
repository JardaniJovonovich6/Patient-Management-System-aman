# **Patient Management System \- Microservices Project**

This repository documents the creation and debugging of a comprehensive, 8-module event-driven microservices application. The project is built using Spring Boot and is fully orchestrated with Docker Compose for local development.

This document also serves as a detailed log of the complex, real-world debugging and learning journey involved in building and deploying this stack, from managing a multi-module Maven build to navigating the complexities of AWS tooling and local cloud simulation.

The final, **successfully deployed** application runs via **Docker Compose**, with all 8 services (including databases and Kafka) orchestrated and communicating.

## **Architecture**

This project consists of 8 interconnected Maven modules:

* **api-gateway**: The single entry point for all client requests. Uses Spring Cloud Gateway to route traffic and a custom filter for JWT-based security.  
* **auth-service**: A dedicated service for user authentication. Issues and validates JSON Web Tokens (JWTs). Connects to its own PostgreSQL database.  
* **patient-service**: Manages patient data (CRUD operations). Connects to the main application database and communicates with other services via gRPC (Billing) and Kafka (Analytics).  
* **billing-service**: Handles patient billing logic. Exposes a gRPC server for the patient-service to create billing accounts. Connects to its own PostgreSQL database.  
* **analytics-service**: A Kafka consumer service that listens to patient topics (e.g., "patient-created" events) and logs them for future analytical processing.  
* **infrastructure**: An AWS CDK (Cloud Development Kit) module that defines the entire cloud infrastructure (VPC, Databases, ECS Clusters, Services) as Java code. (Note: The final configuration requires LocalStack Pro, see learning journey).  
* **integration-tests**: A dedicated module for end-to-end integration testing using RestAssured and Testcontainers.  
* **Root pom.xml**: The parent POM that manages all the modules and their dependencies.

## **Key Technologies**

* **Backend**: Spring Boot, Spring Security (JWT), Spring Data JPA  
* **Orchestration**: Docker, Docker Compose  
* **Database**: PostgreSQL (x3 instances: auth, main app, billing)  
* **Gateway**: Spring Cloud Gateway  
* **Messaging**: Apache Kafka (Event-Driven)  
* **Service-to-Service**: gRPC (Patient-to-Billing)  
* **Infrastructure as Code (IaC)**: AWS CDK, LocalStack, CloudFormation  
* **Testing**: RestAssured, Testcontainers, JUnit 5

## **How to Run This Project (Using Docker Compose)**

This is the recommended and 100% free way to run the entire application on your local machine.

### **Prerequisites**

1. **Git**: To clone the repository.  
2. **Java (JDK 17 or higher)**: To build the Java projects.  
3. **Docker Desktop**: Must be installed and **running** to orchestrate the containers.

### **Step 1: Clone the Repository**

git clone \<your-repository-url\>  
cd patient-management-system

### **Step 2: Build All Microservices**

Before Docker can build the images, it needs the .jar files. This command builds all 8 modules at once from the root.

* **On Windows (PowerShell):**  
  \# Run the wrapper from one of the modules (e.g., patient-service)  
  \# to build everything from the root pom.xml  
  .\\patient-service\\mvnw.cmd clean install

* **On macOS/Linux (Bash):**  
  ./patient-service/mvnw clean install

Wait for this to complete. You should see BUILD SUCCESS for all modules.

### **Step 3: Run Docker Compose**

This single command will read the docker-compose.yaml file, build all 8 images, and start them in the correct order.

docker-compose up \--build

You will see a large stream of logs from all services. This is normal.

### **Step 4: Test the API**

Once the logs calm down, your entire stack is running. The "front door" is the api-gateway at http://localhost:4000.

Open a **new terminal** and test the login endpoint:

curl \-X POST http://localhost:4000/auth/login \\  
\-H "Content-Type: application/json" \\  
\-d "{\\"email\\": \\"user@test.com\\", \\"password\\": \\"password123\\"}"

You should receive a JSON response with your JWT token, proving the api-gateway, auth-service, and auth-service-db are all working.

{"token":"eyJhbGciOiJIUzUxMiJ9..."}

## **My Learning & Debugging Journey (A DevOps Deep Dive)**

This project was a deep dive into solving real-world microservice and DevOps challenges. Here is a log of the key problems I faced and the solutions I implemented.

### **Part 1: Docker Compose & Service Orchestration**

* **Problem**: Services (like auth-service and api-gateway) were crashing on startup.  
* **Investigation**: I traced the logs using docker-compose logs \<service\_name\>.  
* **Solutions**:  
  * **Database Doesn't Exist**: The auth-service failed because its database (auth\_db) wasn't being created.  
    * **Fix**: Added CREATE DATABASE auth\_db; to the db-init/init.sql script.  
  * **Volume Issues**: Realized that init.sql only runs when a Docker volume is *first created*.  
    * **Fix**: Used docker-compose down \-v to destroy the old database volumes and force a clean initialization.  
  * **Configuration Mismatch**: The api-gateway crashed with a PlaceholderResolutionException.  
    * **Fix**: My filter code was looking for auth.service.url, but my docker-compose.yaml defined it as AUTH\_SERVICE\_URL. I corrected the docker-compose.yaml to use the dot-notation key (auth.service.url: ...).

### **Part 2: The Multi-Day Docker Build Failure ("JAR Not Found")**

This was the most complex problem, involving a bug that only appeared inside the Docker build.

* **Symptom**: docker-compose up \--build would fail on the auth-service with the error COPY ... /app/auth-service/target/auth-service-0.0.1-SNAPSHOT.jar: not found.  
* **Problem 1: Maven Project Structure**: My local mvn clean install was not building all modules.  
  * **Fix**: The root pom.xml was missing \<module\>auth-service\</module\> and others. I had to edit each module's pom.xml to add the \<parent\> section, linking it back to the root pom.xml.  
* **Problem 2: Version Mismatch**: The COPY command was still failing.  
  * **Fix**: I finally found a version mismatch. The pom.xml was building version 1.0.0-SNAPSHOT but the docker-compose.yaml (in the SERVICE\_JAR\_PATH argument) was looking for 0.0.1-SNAPSHOT. I standardized all my modules to version 0.0.1-SNAPSHOT to fix the COPY error permanently.

### **Part 3: API Gateway Security & Testing**

* **Problem**: How could login work if the auth-db was empty?  
* **Learning**: I learned that SPRING\_JPA\_HIBERNATE\_DDL\_AUTO: update automatically creates the tables from my User.java entity, and DataSeeder.java (a CommandLineRunner) runs *after* that, populating the users table with a test user (user@test.com) for me to log in with.  
* **Flow**: I successfully implemented a custom JwtValidationGatewayFilterFactory in the api-gateway. The working flow is:  
  1. Client POSTS to /auth/login (via Gateway).  
  2. Gateway routes to auth-service, which returns a JWT.  
  3. Client GETS /patients (via Gateway) with the Authorization: Bearer \<token\> header.  
  4. The JwtValidation filter intercepts this and makes a background call to /auth/validate on the auth-service.  
  5. auth-service validates the token's signature and returns 200 OK.  
  6. The filter allows the original request to proceed to the patient-service.

### **Part 4: The cdk synth (Build) Gauntlet**

My first attempt at the infrastructure module was to build the CloudFormation template. This failed for multiple, complex tooling reasons.

* **Problem**: mvnw: command not found and cdk: command not found.  
  * **Fix**: I learned my Maven wrappers were in sub-folders, not the root. I had to run builds from the root by calling a child's wrapper (e.g., .\\patient-service\\mvnw.cmd ...). I also had to add the npm global directory to my Windows PATH to find the cdk command.  
* **Problem**: cdk synth failed with a Java error (ClassNotFoundException: com.pm.stack.Main).  
  * **Fix**: The cdk.json was telling Maven to exec:java on *all* modules. I fixed the command to target *only* the infrastructure module: ... \-pl infrastructure exec:java ....  
* **Problem**: cdk synth failed with NoSuchMethodException: com.pm.stack.LocalStack.main(String\[\]).  
  * **Fix**: I was telling Maven to run the LocalStack.java class, but my main method was correctly in Main.java. I fixed this by adding the \<mainClass\>com.pm.stack.Main\</mainClass\> configuration to the exec-maven-plugin in the infrastructure/pom.xml.

### **Part 5: The localstack-deploy.sh (Deploy) Hell**

After finally generating the localstack.template.json, the deployment script failed for an entirely new set of reasons. This was a multi-day war against the AWS CLI.

* **Problem**: aws.cmd: command not found in my Git Bash terminal.  
  * **Investigation**: My terminal could not find the aws.exe program I had installed. Adding it to the PATH variable in \~/.bash\_profile was not working.  
  * **Fix**: This was a deep Windows/Git Bash issue. PATH was not the solution. I had to create a permanent **alias** in my \~/.bash\_profile (alias aws='"/c/Program Files/Amazon/AWSCLIV2/aws.exe"') to force the Bash shell to find the Windows executable.  
* **Problem**: An error occurred (InvalidAccessKeyId)...  
  * **Investigation**: This was the biggest struggle. My CLI, now working via the alias, was sending my *real* AWS credentials to LocalStack, which (correctly) rejected them. Trying to export "test" keys didn't work, and even moving my \~/.aws folder failed.  
  * **Fix**: The AWS CLI was reading credentials from *multiple* places. The final, correct solution was to run aws configure in my **Windows Command Prompt (cmd)** and set the **default** credentials to test, test, and us-east-1. This created the *global* \~/.aws/credentials file that LocalStack would accept.  
* **Problem**: The script was corrupted with GNU: command not found errors.  
  * **Investigation**: The script I copied from the web had invisible "rich text" characters.  
  * **Fix**: I had to manually delete and re-type the script in nano to ensure it was a clean text file.  
* **Problem**: S3 Bucket does not exist... followed by InvalidAccessKeyId when I tried to create it manually.  
  * **Investigation**: My *manual* aws s3 mb command was talking to the *real* AWS, while my script was (correctly) talking to LocalStack.  
  * **Fix**: I had to put the aws s3 mb s3://localstack-cf-templates || true command **inside** the localstack-deploy.sh script, so it would run within the "magic bubble" of export variables pointing to http://localhost:4566.

### **Part 6: The Final Boss: The LocalStack Paywall**

* **Problem**: After fixing *everything*, the deployment still failed.  
* **Investigation**: I ran aws cloudformation describe-stack-events... (after setting the export variables manually) and found the real error.  
* **The Error**: plugin localstack.aws.provider:elbv2:pro is disabled and plugin localstack.aws.provider:ecs:pro is disabled.  
* **Conclusion**: The tutorial's infrastructure module is **fundamentally incompatible with the free version of LocalStack.** It relies on paid "Pro" features (Application Load Balancers and ECS). I successfully proved this by modifying the Java code to remove the load balancer, which then revealed the *next* paywall (ECS).

### **Part 7: The Real Success: Docker Compose Deployment**

This entire journey confirmed that the correct, free, and standard way to run this stack locally is **Docker Compose.**

The docker-compose.yaml file orchestrates all 8 containers, and as of this commit, **the entire application stack is 100% deployed and functional** on http://localhost:4000.

This project taught me more about debugging real-world environment, tooling, and build-system issues than any tutorial could. It proved that understanding *why* something is failing (by reading logs and testing one variable at a time) is the most important skill in DevOps.

## **Future Vision & Next Steps**

This repository serves as a robust, production-ready foundation for a full-scale, multi-tenant SaaS (Software as a Service) application. My future plans include:

* **Add a Frontend**: Build a React (Vite) or Angular frontend that consumes the api-gateway.  
* **Implement User Roles**: Expand the auth-service to include "ADMIN" vs. "USER" roles, restricting endpoint access.  
* **Full SaaS Implementation**: Add a "Tenant" concept so that multiple hospitals or clinics can use the same application, with all data isolated by their tenant ID.  
* **Enhance Analytics**: Actually *use* the Kafka events to build a real-time analytics dashboard for the "ADMIN" users.  
* **Real Cloud Deployment**: Take what I learned and deploy this stack to a real AWS account using their free tier services (e.g., using AWS Fargate, RDS, and MSK Serverless).