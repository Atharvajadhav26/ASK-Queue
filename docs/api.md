# Smart Queue System — REST API Reference

**Base URL:** `/api`  
**Content-Type:** `application/json`  
**Auth:** `Authorization: Bearer <JWT_TOKEN>`

## Error Response Format
```json
{
  "status": 400,
  "error": "Queue Full",
  "message": "Queue has reached maximum capacity",
  "timestamp": "2026-08-10T12:00:00"
}
```

---

## 1. Authentication

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/auth/register` | Public | Register new account |
| POST | `/auth/login` | Public | Login, receive JWT |
| GET | `/auth/me` | Token | Get current user info |

**POST /auth/register**
```json
// Request
{ "name": "John", "email": "john@mail.com", "mobile": "9876543210", "password": "Pass@123" }
// Response (200)
{ "token": "eyJ...", "userId": 1, "name": "John", "email": "john@mail.com", "role": "CUSTOMER" }
```

**POST /auth/login**
```json
// Request
{ "email": "john@mail.com", "password": "Pass@123" }
// Response (200)
{ "token": "eyJ...", "userId": 1, "name": "John", "email": "john@mail.com", "role": "CUSTOMER" }
```

---

## 2. Public Queue

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/queues/{queueId}` | Public | Get queue details (by string ID like Q-8F42A1) |

---

## 3. Customer Tokens

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/queues/{queueId}/join` | Token | Join a queue |
| GET | `/tokens/active` | Token | Get active token(s) |
| GET | `/tokens/history` | Token | Get completed/cancelled tokens |
| POST | `/tokens/{id}/hold` | Token | Hold token (owner only) |
| POST | `/tokens/{id}/resume` | Token | Resume held token |
| POST | `/tokens/{id}/skip` | Token | Skip positions `{ "positions": 3 }` |
| POST | `/tokens/{id}/leave` | Token | Leave queue (cancel token) |

---

## 4. Admin Queues

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/admin/queues` | Admin | List admin's queues |
| POST | `/admin/queues` | Admin | Create queue |
| PUT | `/admin/queues/{id}` | Admin | Update queue config |
| POST | `/admin/queues/{id}/open` | Admin | Open queue for joins |
| POST | `/admin/queues/{id}/close` | Admin | Close queue |
| GET | `/admin/queues/{id}/tokens` | Admin | Get all tokens in queue |
| POST | `/admin/queues/{id}/next` | Admin | Call next customer |
| POST | `/admin/queues/{id}/previous` | Admin | Revert to previous |
| GET | `/admin/queues/{id}/qrcode` | Admin | Get QR code (image/png) |

**POST /admin/queues — Create Queue**
```json
// Request
{
  "name": "General OPD",
  "serviceType": "Clinic",
  "description": "Outpatient department queue",
  "prefix": "OPD",
  "maxCapacity": 100,
  "avgTimePerPerson": 7,
  "holdTimeMinutes": 15,
  "workingHoursStart": "09:00",
  "workingHoursEnd": "17:00"
}
```

---

## 5. Admin Token Actions

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/admin/tokens/{id}/hold` | Admin | Admin hold token |
| POST | `/admin/tokens/{id}/resume` | Admin | Admin resume token |
| POST | `/admin/tokens/{id}/skip` | Admin | Admin skip `{ "positions": N }` |
| POST | `/admin/tokens/{id}/cancel` | Admin | Cancel with `{ "reason": "..." }` |
| POST | `/admin/tokens/{id}/complete` | Admin | Complete service |
| POST | `/admin/tokens/{id}/priority` | Admin | Set priority `{ "priority": "VIP", "reason": "..." }` |

---

## 6. Analytics & Reports

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/admin/analytics/dashboard` | Admin | Dashboard statistics |
| GET | `/admin/analytics/charts` | Admin | Chart data (hourly, daily, distributions) |
| GET | `/admin/reports/pdf?period=daily` | Admin | Download PDF report |
| GET | `/admin/reports/excel?period=weekly` | Admin | Download Excel report |

Period values: `daily`, `weekly`, `monthly`

---

## 7. Notifications

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/notifications` | Token | Get user's notifications |
| GET | `/notifications/count` | Token | Get unread count |
| PUT | `/notifications/{id}/read` | Token | Mark as read |
| PUT | `/notifications/read-all` | Token | Mark all as read |

---

## 8. Profile

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/profile` | Token | Get profile data |
| PUT | `/profile` | Token | Update profile, preferences, password |
