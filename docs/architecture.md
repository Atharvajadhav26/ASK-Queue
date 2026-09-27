# Smart Queue System — Architecture

## Overview

3-tier architecture: React frontend → Spring Boot backend → MySQL database.

```
┌──────────────────────────────┐
│       REACT FRONTEND         │
│  Vite, Bootstrap 5, STOMP.js │
└──────────┬───────────────────┘
           │  REST API / WebSocket (STOMP)
           ▼
┌──────────────────────────────┐
│     SPRING BOOT BACKEND      │
│  MVC, Security, JPA, WS     │
│                              │
│  ┌────────────────────────┐  │
│  │     QueueEngine        │  │
│  │  (Core Business Logic) │  │
│  └────────────────────────┘  │
└──────────┬───────────────────┘
           │  JDBC / Hibernate
           ▼
┌──────────────────────────────┐
│       MySQL 8.0+             │
│  InnoDB, Pessimistic Locks   │
└──────────────────────────────┘
```

## Backend Layer Structure

| Package | Responsibility |
|---------|---------------|
| `controller/` | HTTP endpoints, request validation, response mapping |
| `service/` | Business logic (QueueEngine, QueueService, AuthService, etc.) |
| `repository/` | Spring Data JPA interfaces, custom queries, pessimistic locks |
| `entity/` | JPA entities mapped to MySQL tables |
| `dto/` | Request DTOs (with validation) and Response DTOs |
| `mapper/` | Static mappers converting entities ↔ DTOs |
| `security/` | JWT provider, auth filter, SecurityConfig, UserPrincipal |
| `config/` | WebSocket, CORS, Scheduler, DataSeeder configs |
| `websocket/` | WebSocket event listener, broadcast service |
| `notification/` | Strategy-pattern notification providers (Mock, FCM, Twilio) |
| `scheduler/` | Hold expiry processing (runs every 30s) |
| `exception/` | Custom exceptions + GlobalExceptionHandler |

## Key Design Decisions

1. **QueueEngine as Single Source of Truth** — All queue-mutating operations go through `QueueEngine`. Controllers and frontend never directly modify queue state.

2. **Stateless JWT Auth** — No server-side sessions. Every request carries a JWT Bearer token.

3. **Pessimistic + Optimistic Locking** — `SELECT ... FOR UPDATE` on queue records for atomic next/join operations. `@Version` on tokens for concurrent update detection.

4. **WebSocket for Real-Time** — STOMP over SockJS provides real-time queue updates. Backend broadcasts events; frontend subscribes to topics.

5. **Strategy Pattern for Notifications** — `NotificationProvider` interface with Mock, FCM, and WhatsApp implementations. `NotificationDispatcher` routes to available providers.
