# Smart Queue System

**Digital Token & Live Queue Management Platform**

A production-grade, full-stack web application for digitally managing queues across various service sectors — clinics, hospitals, banks, salons, college offices, government offices, and service centers.

---

## Problem Statement

Traditional queue management involves physical waiting, uncertainty about wait times, and no visibility into queue position. This leads to poor customer experience, wasted time, and inefficient service delivery.

## Solution

Smart Queue System eliminates physical waiting by providing:
- **Digital token generation** — customers join queues remotely
- **Live position tracking** — real-time updates on queue position
- **Smart notifications** — alerts when turn is approaching
- **Administrative control** — complete queue lifecycle management
- **Analytics & reporting** — data-driven insights for service improvement

---

## Features

### Customer Features
- Register/Login with secure JWT authentication
- Join queues via QR Code, Queue ID, or direct link
- Live queue position tracking with real-time WebSocket updates
- Hold/Resume tokens for temporary absence
- Skip positions voluntarily
- Receive notifications (5 ahead, 3 ahead, 1 ahead, your turn)
- View complete queue history
- Multi-language support (English, Hindi, Marathi)

### Admin Features
- Create and configure queues with customizable settings
- Auto-generated Queue IDs, QR Codes, and shareable links
- Call Next / Previous customer
- Hold, Resume, Skip, Cancel, Complete tokens
- Set priority (Normal, VIP, Emergency)
- Real-time queue management dashboard
- Analytics with Chart.js visualizations
- PDF and Excel report generation
- Concurrent operation safety with database locking

---

## Technology Stack

| Layer | Technologies |
|-------|-------------|
| **Frontend** | React 18, Vite, React Router v7, Bootstrap 5, Axios, Chart.js, react-i18next, STOMP.js, qrcode.react |
| **Backend** | Java 17, Spring Boot 3.4, Spring MVC, Spring Security 6, Spring Data JPA, Hibernate, Maven |
| **Database** | MySQL 8.0 |
| **Security** | JWT (JJWT), BCrypt, Role-Based Access Control |
| **Real-time** | Spring WebSocket + STOMP, SockJS |
| **QR Code** | ZXing (backend), qrcode.react (frontend) |
| **Reports** | Apache POI (Excel), OpenPDF (PDF) |
| **Notifications** | Firebase Cloud Messaging, Twilio WhatsApp API, Mock Provider |

---

## Architecture

```
┌─────────────┐      ┌──────────────────┐      ┌─────────┐
│   React UI  │◄────►│  Spring Boot API  │◄────►│  MySQL  │
│  (Vite)     │      │  (REST + WS)      │      │   DB    │
└─────────────┘      └──────────────────┘      └─────────┘
       ▲                      │
       │                      ▼
   WebSocket           ┌──────────────┐
   (STOMP/SockJS)      │ QueueEngine  │
                       │ (Core Logic) │
                       └──────────────┘
```

### Backend Architecture
```
controller/     → HTTP request handling
service/        → Business logic
  QueueEngine   → Core queue algorithms
repository/     → Data access (JPA)
entity/         → JPA entities
dto/            → Request/Response objects
security/       → JWT + Spring Security
config/         → App configuration
websocket/      → Real-time events
notification/   → FCM, Twilio, Mock providers
scheduler/      → Hold expiry processing
exception/      → Global error handling
```

---

## Database Design

### Core Entities
- **users** — Customer and admin accounts
- **service_queues** — Queue definitions with unique IDs
- **queue_settings** — Configurable parameters per queue
- **tokens** — Customer tokens with state machine lifecycle
- **token_history** — Full audit trail of state transitions
- **notifications** — In-app and push notification records

### Token State Machine
```
WAITING → HELD (hold) → WAITING (resume) / EXPIRED (timeout)
WAITING → SERVING (next) → COMPLETED (complete)
WAITING → CANCELLED (cancel/leave)
WAITING → SKIPPED (skip)
```

---

## Setup Instructions

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0+
- Node.js 18+
- npm 9+

### Backend Setup

1. **Create MySQL database:**
```sql
CREATE DATABASE smart_queue CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. **Configure environment:**
```bash
cd backend
# Edit src/main/resources/application.yml or set environment variables:
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=smart_queue
export DB_USERNAME=root
export DB_PASSWORD=your_password
export JWT_SECRET=your-secret-key
```

3. **Build and run:**
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The backend starts on `http://localhost:8080`.

### Frontend Setup

1. **Install dependencies:**
```bash
cd frontend
npm install
```

2. **Configure environment:**
```bash
cp .env.example .env
# Edit .env if needed
```

3. **Run development server:**
```bash
npm run dev
```

The frontend starts on `http://localhost:5173`.

### Default Admin Account
- **Email:** admin@smartqueue.com
- **Password:** Admin@123

> ⚠️ Change these credentials in production!

---

## API Documentation

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new customer |
| POST | `/api/auth/login` | Login and receive JWT |
| GET | `/api/auth/me` | Get current user info |

### Queue Operations (Customer)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/queues/{queueId}` | Get queue details (public) |
| POST | `/api/queues/{queueId}/join` | Join a queue |
| GET | `/api/tokens/active` | Get active token |
| GET | `/api/tokens/history` | Get token history |
| POST | `/api/tokens/{id}/hold` | Hold token |
| POST | `/api/tokens/{id}/resume` | Resume token |
| POST | `/api/tokens/{id}/skip` | Skip positions |
| POST | `/api/tokens/{id}/leave` | Leave queue |

### Admin Operations
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/queues` | List admin queues |
| POST | `/api/admin/queues` | Create queue |
| POST | `/api/admin/queues/{id}/open` | Open queue |
| POST | `/api/admin/queues/{id}/close` | Close queue |
| POST | `/api/admin/queues/{id}/next` | Call next token |
| POST | `/api/admin/queues/{id}/previous` | Revert to previous |
| POST | `/api/admin/tokens/{id}/complete` | Complete service |
| POST | `/api/admin/tokens/{id}/priority` | Set priority |

### Analytics & Reports
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/analytics/dashboard` | Dashboard statistics |
| GET | `/api/admin/analytics/charts` | Chart data |
| GET | `/api/admin/reports/pdf?period=daily` | Download PDF report |
| GET | `/api/admin/reports/excel?period=daily` | Download Excel report |

---

## Queue Algorithm

The **QueueEngine** is the single source of truth for all queue operations:

1. **Token Generation**: Atomic sequence increment, position assignment
2. **Next Token**: Pessimistic locking, priority ordering (EMERGENCY > VIP > NORMAL), position-based selection
3. **Previous Token**: History-based reversal with state validation
4. **Concurrency**: Database-level pessimistic locks + optimistic locking (`@Version`)
5. **Position Calculation**: Dense-rank recalculation after every operation
6. **Wait Estimation**: People ahead × average time per person

---

## WebSocket Events

| Topic | Description |
|-------|-------------|
| `/topic/queue/{queueId}` | Queue state changes (all users) |
| `/topic/admin/queue/{queueId}` | Admin-specific updates |
| `/user/{userId}/queue/notifications` | Personal notifications |

---

## Environment Variables

See `.env.example` for all configurable values:
- Database connection (DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD)
- JWT configuration (JWT_SECRET, JWT_EXPIRATION_MS)
- Firebase FCM credentials
- Twilio WhatsApp credentials
- Frontend/CORS URLs

---

## Deployment

### Frontend (Vercel/Netlify)
```bash
cd frontend
npm run build
# Deploy the `dist` directory
```

### Backend (Render/Railway/AWS)
```bash
cd backend
mvn clean package -DskipTests
# Deploy the JAR from target/
java -jar target/smart-queue-system-1.0.0.jar
```

### Production Checklist
- [ ] Change JWT secret
- [ ] Change default admin password
- [ ] Configure CORS for production frontend URL
- [ ] Set up cloud MySQL
- [ ] Configure FCM credentials (optional)
- [ ] Configure Twilio credentials (optional)
- [ ] Enable HTTPS

---

## License

MIT
