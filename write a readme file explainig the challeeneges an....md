# **Patient Management System \- A Modern Microservice Project**

**Status:** In Development | **Current Focus:** API Gateway & Security Implementation

## **Introduction**

This project is my hands-on journey into building a modern, resilient, and scalable Patient Management System from the ground up. It's designed as a multi-module microservice application, moving beyond monolithic design to embrace a distributed architecture. The core of this project is not just the application itself, but the robust development and deployment workflow built around **Docker**, **gRPC**, and **Apache Kafka**.

The system is currently composed of three core services that work in concert:

* **Patient Service:** A public-facing RESTful API for all patient-related CRUD operations. It acts as the primary data owner for patient information and the main producer of events.  
* **Billing Service:** An internal-only service that handles billing logic. It communicates synchronously with the Patient Service via high-performance gRPC.  
* **Analytics Service:** A decoupled service that listens to asynchronous events from the Patient Service via Kafka, designed for future data analysis and reporting without impacting core operations.

## **Architecture Overview**

The architecture is designed to be decoupled and event-driven, combining both synchronous and asynchronous communication patterns for the right use case.

* **Internal Communication:**  
  * **Synchronous (gRPC):** For immediate, required interactions (like creating a billing account when a patient signs up), the system uses gRPC. This ensures a fast, strongly-typed contract between the patient-service and the billing-service.  
  * **Asynchronous (Apache Kafka):** For decoupled, non-blocking communication, the system uses Kafka. When a new patient is created, the patient-service publishes a PatientEvent. The analytics-service (and any future service) can subscribe to this event to perform its own logic without the patient-service needing to know or wait for it.  
* **External Communication (Future):**  
  * **API Gateway:** All external traffic from the frontend will be routed through a single API Gateway (to be built with Spring Cloud Gateway). This will act as the single "front door," handling routing, security, and abstracting the complexity of the internal microservices.

## **Technology Stack**

| Category | Technologies |
| :---- | :---- |
| **Backend** | Java 17, Spring Boot, Spring Data JPA, Spring Web |
| **Database** | PostgreSQL, H2 (for testing) |
| **Communication** | REST, gRPC (with Protocol Buffers), Apache Kafka |
| **DevOps** | Docker, Docker Compose, Maven (Multi-Module) |
| **Future Frontend** | React |

## **The Journey: Challenges & Key Learnings**

Building a distributed system from scratch is never a straight line. This project's real value comes from the numerous real-world hurdles I had to overcome. This wasn't just about writing code; it was about debugging the entire stack.

Some of the key challenges I solved include:

* **The "Phantom Build" Nightmare:** The most persistent challenge was wrestling with Docker's aggressive build cache. I encountered numerous situations where my Java code changes were not being reflected in the running container. I mastered several cache-busting techniques, from docker system prune to the reliable **"POM Comment" trick**, to guarantee that my latest code was always deployed. This taught me a critical lesson in the realities of containerized development.  
* **Environment & Network Instability:** I faced and solved environment-specific problems that weren't in any tutorial. This included:  
  * Diagnosing and fixing local **DNS resolution failures** that prevented Maven from downloading dependencies.  
  * Troubleshooting TLS handshake timeout errors with Docker Hub, which required a full restart and cache cleanup of the Docker environment itself.  
* **Multi-Module Maven Hell:** Integrating the services into a parent-child Maven structure was a battle. I debugged ClassNotFoundException errors by enforcing a top-down build from the root directory, learning firsthand how crucial build order and inter-module dependencies are.  
* **Spring Boot & Kafka Configuration:** Getting the producer and consumer to talk was a deep dive into Spring's configuration. I debugged ConflictingBeanDefinitionException errors from naming collisions and, most importantly, a ClassCastException in the consumer. This forced me to create a programmatic KafkaConsumerConfig to explicitly define the ByteArrayDeserializer, ensuring the Protobuf message was delivered as raw bytes, not a garbled String.

## **Current Status & Future Plans**

The project has reached a major milestone: the core architecture is built and functional. The patient-service can successfully publish an event, and the analytics-service can successfully consume and parse it.

My roadmap for the future is focused on building out the remaining pieces of a professional, production-ready system:

1. **Implement the API Gateway:** Build a new api-gateway service using Spring Cloud Gateway to act as the single entry point for all frontend requests.  
2. **Build an Authentication Service:** Create a dedicated microservice to handle user registration and login.  
3. **Implement JWT Security:** Integrate Spring Security and JSON Web Tokens (JWT). The API Gateway will be responsible for validating tokens on all incoming requests, securing the entire system from the outside.  
4. **Develop a React Frontend:** Build a modern, responsive user interface to interact with the system via the API Gateway.  
5. **Expand Open Source Contributions:** With a stable backend, I plan to actively participate in the open-source community, contributing to the libraries that made this project possible.

## **How to Run**

1. Ensure you have Docker and Docker Compose installed.  
2. Clone the repository.  
3. From the root directory, run ./mvnw.cmd clean install to build all the modules.  
4. Run docker-compose up \--build to start the entire system.

This project is a living document of my journey as a developer. I'm excited about where it's headed and welcome any feedback or contributions.