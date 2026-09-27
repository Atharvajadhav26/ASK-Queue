# Smart Queue System — Database Design

## Overview

- **Engine:** MySQL 8.0+ (InnoDB)
- **Charset:** utf8mb4 / utf8mb4_unicode_ci
- **Tables:** 6 core tables

## Entity Relationship

```
users ──────┬──── service_queues ──── queue_settings
            │         │
            │         │
            └─── tokens ──── token_history
            │
            └─── notifications
```

## Tables

### 1. `users`

| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT |
| name | VARCHAR(100) | NOT NULL |
| email | VARCHAR(150) | UNIQUE, NOT NULL |
| mobile | VARCHAR(20) | UNIQUE, NOT NULL |
| password | VARCHAR(255) | NOT NULL (BCrypt) |
| role | ENUM('CUSTOMER','ADMIN') | DEFAULT 'CUSTOMER' |
| preferred_language | VARCHAR(10) | DEFAULT 'en' |
| notification_email | BOOLEAN | DEFAULT TRUE |
| notification_sms | BOOLEAN | DEFAULT TRUE |
| notification_push | BOOLEAN | DEFAULT TRUE |
| fcm_token | VARCHAR(500) | NULLABLE |
| created_at | DATETIME | AUTO |
| updated_at | DATETIME | AUTO |

### 2. `service_queues`

| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT |
| queue_id | VARCHAR(20) | UNIQUE, NOT NULL (e.g. Q-8F42A1) |
| name | VARCHAR(150) | NOT NULL |
| service_type | VARCHAR(50) | NOT NULL |
| description | TEXT | NULLABLE |
| prefix | VARCHAR(10) | DEFAULT 'Q' |
| status | ENUM('OPEN','CLOSED','PAUSED') | DEFAULT 'CLOSED' |
| current_token_seq | INT | DEFAULT 0 |
| current_serving_token_id | BIGINT | NULLABLE FK → tokens |
| created_by | BIGINT | NOT NULL FK → users |
| created_at, updated_at | DATETIME | AUTO |

### 3. `queue_settings`

| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PK |
| queue_id | BIGINT | UNIQUE FK → service_queues ON DELETE CASCADE |
| max_capacity | INT | DEFAULT 50 |
| avg_time_per_person_minutes | INT | DEFAULT 5 |
| hold_time_minutes | INT | DEFAULT 10 |
| working_hours_start | VARCHAR(10) | DEFAULT '09:00' |
| working_hours_end | VARCHAR(10) | DEFAULT '17:00' |
| hold_expiry_behavior | VARCHAR(20) | DEFAULT 'EXPIRE' |

### 4. `tokens`

| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PK |
| token_number | VARCHAR(30) | NOT NULL (e.g. Q-1001) |
| queue_id | BIGINT | FK → service_queues |
| user_id | BIGINT | FK → users |
| status | ENUM | DEFAULT 'WAITING' |
| priority | ENUM('NORMAL','VIP','EMERGENCY') | DEFAULT 'NORMAL' |
| priority_reason | VARCHAR(255) | NULLABLE |
| queue_position | INT | DEFAULT 0 |
| join_time | DATETIME | NOT NULL |
| called_time, serving_start_time, completed_time, cancelled_time | DATETIME | NULLABLE |
| hold_start_time, hold_expiry_time | DATETIME | NULLABLE |
| skip_count | INT | DEFAULT 0 |
| version | INT | DEFAULT 0 (optimistic lock) |

**Key Indexes:**
- `idx_tokens_queue_status` — (queue_id, status) for active token lookups
- `idx_tokens_queue_position` — (queue_id, queue_position) for ordering
- `idx_tokens_user_status` — (user_id, status) for user active token check
- `idx_tokens_hold_expiry` — (hold_expiry_time) for scheduler
- `idx_tokens_queue_priority` — (queue_id, priority, queue_position) for priority sorting

### 5. `token_history`

Full audit trail of every state transition. Fields: token_id (FK), from_status, to_status, action, action_by, action_by_id, details, position_before, position_after, created_at.

### 6. `notifications`

In-app notification records. Fields: user_id (FK), token_id, queue_id, title, message, type (enum), is_read, channel, created_at.

## Token State Machine

```
         ┌─────────┐
    ┌────│ WAITING  │────────────────────┐
    │    └────┬────┘                     │
    │         │                          │
    │    hold │  next                cancel/leave
    │         ▼                          │
    │    ┌─────────┐                     ▼
    │    │  HELD   │──expire──→ EXPIRED  CANCELLED
    │    └────┬────┘
    │    resume│
    │         ▼
    │    ┌─────────┐
    └───→│ SERVING │──complete──→ COMPLETED
         └─────────┘
```

## Concurrency Control

1. **Pessimistic Lock:** `SELECT ... FOR UPDATE` on `service_queues` for join/next/previous — serializes atomic operations.
2. **Optimistic Lock:** `@Version` on `tokens` table detects concurrent modifications.
3. **@Transactional:** All QueueEngine methods run within database transactions with automatic rollback.
