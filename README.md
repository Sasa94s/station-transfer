# 🚀 Station Transfer Service

A Spring Boot application that ingests transfer events between stations and provides aggregated summaries per station.

---

## 📌 Overview

This service processes transfer events and maintains station-level summaries. It is designed to handle:

- Batch event ingestion
- Duplicate event handling (idempotency)
- Out-of-order event arrival
- Concurrent processing
- Station summary retrieval

---

## 🏗️ Architecture

The application follows a layered architecture:

Controller → Service → Repository → In-Memory Storage

### Key Components

#### Controller
- `TransferController`  
  Exposes REST APIs for ingestion and summary retrieval

#### Service
- `TransferService`  
  Core business logic (deduplication, aggregation)

#### Repository
- `TransferRepository` (interface)
- `InMemoryTransferRepository`  
  In-memory implementation for storage

#### Models
- `TransferEvent` → Internal entity
##### DTOs
- `EventDto` → Incoming event payload
##### Request / Response Beans
- `TransferRequest` → Batch request wrapper
- `TransferResponse` → Response for ingestion
- `StationSummaryResponse` → Aggregated station data

#### Exception Handling
- `GlobalExceptionHandler`  
  Centralized error handling

---

## ⚙️ Tech Stack

- Java 26
- Spring Boot
- Maven
- Docker & Docker Compose
- H2 (in-memory DB)

---

## 📥 API Endpoints

### 1. Ingest Transfer Events

**POST** `/transfers`

#### Request
```json
{
  "events": [
    {
      "event_id": "uuid-1",
      "station_id": "station-1",
      "type": "approved",
      "value": 10,
      "timestamp": "2026-01-01T10:00:00Z"
    }
  ]
}

#### Response
```json
{
  "inserted": 1,
  "duplicates": 0
}
```

### 2. Get Station Summary
```
GET /stations/{stationId}/summary
```

#### Response
```json
{
  "stationId": "station-1",
  "totalIn": 100,
  "totalOut": 50,
  "net": 50
}
```

### 🧠 Business Rules
- ✅ Idempotency
Duplicate event_id values are ignored.
- ✅ Order Independence
Events can arrive out of order without affecting correctness.
- ✅ Concurrency Safety
Concurrent ingestion of the same event does not double count.
- ✅ Validation
- Missing or invalid fields are rejected
- Handled via GlobalExceptionHandler

### 🧪 Testing Strategy

Recommended test coverage:
1.	Batch insert correctness
2.	Duplicate event handling
3.	Out-of-order processing
4.	Concurrent ingestion safety
5.	Station summary accuracy
6.	Validation failure scenarios


### 🐳 Running with Docker

Build & Run
```json
docker-compose up --build
```
App will be available at:
```json
http://localhost:8080
```

### 🛠️ Running Locally

Prerequisites
- Java 17+
- Maven

Steps
```json
mvn clean install
mvn spring-boot:run
```

#### 📂 Project Structure
```json
src/main/java/com/petroapp/stationtransfer/

├── controllers/
│   └── TransferController.java
│
├── services/
│   └── TransferService.java
│
├── repositories/
│   ├── TransferRepository.java
│   └── InMemoryTransferRepository.java
│
├── models/
│   ├── entities/
│   │   └── TransferEvent.java
│   ├── dtos/
│   │   └── EventDto.java
│   └── requests/responses/
│       ├── TransferRequest.java
│       ├── TransferResponse.java
│       └── StationSummaryResponse.java
│
├── exceptions/
│   └── GlobalExceptionHandler.java
│
└── StationTransferApplication.java
```

## ⚠️ Known Limitations
•	In-memory storage (data lost on restart)
•	No persistence layer (can be extended to DB)
•	No authentication/authorization

## 🚧 Possible Improvements
•	Introduce caching (Redis)
•	Add metrics & monitoring
•	Improve concurrency handling with locks or atomic structures
