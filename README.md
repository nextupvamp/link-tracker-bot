# Link Tracker

**Content Update Tracking Application**  
The application tracks content updates via URLs and sends Telegram notifications when new events occur.

The project is written in `Java 23` using `Spring Boot 3`.

### Project Structure
The project consists of two applications:
- Bot
- Scrapper

### Required Services
- PostgreSQL
- Redis
- Kafka

### Build and Run
- Build the project:  
`mvn clean verify`  
- Build Docker images:  
`docker-compose build`  
- Start the services:  
`docker-compose up`  
