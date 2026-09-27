-- ============================================================
-- SMART QUEUE SYSTEM — MySQL Database Schema
-- ============================================================
-- Version: 1.0
-- Database: MySQL 8.0+
-- ============================================================

CREATE DATABASE IF NOT EXISTS smart_queue
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smart_queue;

-- ============================================================
-- USERS TABLE
-- ============================================================
CREATE TABLE users (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    email           VARCHAR(150)    NOT NULL,
    mobile          VARCHAR(20)     NOT NULL,
    password        VARCHAR(255)    NOT NULL,
    role            ENUM('CUSTOMER', 'ADMIN') NOT NULL DEFAULT 'CUSTOMER',
    preferred_language VARCHAR(10)  NOT NULL DEFAULT 'en',
    notification_email BOOLEAN      NOT NULL DEFAULT TRUE,
    notification_sms   BOOLEAN      NOT NULL DEFAULT TRUE,
    notification_push  BOOLEAN      NOT NULL DEFAULT TRUE,
    fcm_token       VARCHAR(500)    NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_users_email  UNIQUE (email),
    CONSTRAINT uk_users_mobile UNIQUE (mobile)
) ENGINE=InnoDB;

CREATE INDEX idx_users_role ON users(role);

-- ============================================================
-- SERVICE QUEUES TABLE
-- ============================================================
CREATE TABLE service_queues (
    id                      BIGINT          AUTO_INCREMENT PRIMARY KEY,
    queue_id                VARCHAR(20)     NOT NULL,
    name                    VARCHAR(150)    NOT NULL,
    service_type            VARCHAR(50)     NOT NULL,
    description             TEXT            NULL,
    prefix                  VARCHAR(10)     NOT NULL DEFAULT 'Q',
    status                  ENUM('OPEN', 'CLOSED', 'PAUSED') NOT NULL DEFAULT 'CLOSED',
    current_token_seq       INT             NOT NULL DEFAULT 0,
    current_serving_token_id BIGINT         NULL,
    created_by              BIGINT          NOT NULL,
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_service_queues_queue_id UNIQUE (queue_id),
    CONSTRAINT fk_service_queues_created_by FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE INDEX idx_service_queues_status ON service_queues(status);
CREATE INDEX idx_service_queues_created_by ON service_queues(created_by);

-- ============================================================
-- QUEUE SETTINGS TABLE
-- ============================================================
CREATE TABLE queue_settings (
    id                          BIGINT      AUTO_INCREMENT PRIMARY KEY,
    queue_id                    BIGINT      NOT NULL,
    max_capacity                INT         NOT NULL DEFAULT 50,
    avg_time_per_person_minutes INT         NOT NULL DEFAULT 5,
    hold_time_minutes           INT         NOT NULL DEFAULT 10,
    working_hours_start         VARCHAR(10) NOT NULL DEFAULT '09:00',
    working_hours_end           VARCHAR(10) NOT NULL DEFAULT '17:00',
    hold_expiry_behavior        ENUM('EXPIRE', 'MOVE_TO_END') NOT NULL DEFAULT 'EXPIRE',
    created_at                  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_queue_settings_queue FOREIGN KEY (queue_id) REFERENCES service_queues(id) ON DELETE CASCADE,
    CONSTRAINT uk_queue_settings_queue UNIQUE (queue_id)
) ENGINE=InnoDB;

-- ============================================================
-- TOKENS TABLE
-- ============================================================
CREATE TABLE tokens (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    token_number        VARCHAR(30)     NOT NULL,
    queue_id            BIGINT          NOT NULL,
    user_id             BIGINT          NOT NULL,
    status              ENUM('WAITING', 'HELD', 'CALLED', 'SERVING', 'SKIPPED', 'CANCELLED', 'COMPLETED', 'EXPIRED')
                                        NOT NULL DEFAULT 'WAITING',
    priority            ENUM('NORMAL', 'VIP', 'EMERGENCY') NOT NULL DEFAULT 'NORMAL',
    priority_reason     VARCHAR(255)    NULL,
    priority_assigned_by BIGINT         NULL,
    queue_position      INT             NOT NULL DEFAULT 0,
    join_time           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    called_time         DATETIME        NULL,
    serving_start_time  DATETIME        NULL,
    completed_time      DATETIME        NULL,
    cancelled_time      DATETIME        NULL,
    hold_start_time     DATETIME        NULL,
    hold_expiry_time    DATETIME        NULL,
    cancelled_by        BIGINT          NULL,
    cancel_reason       VARCHAR(255)    NULL,
    skip_count          INT             NOT NULL DEFAULT 0,
    version             INT             NOT NULL DEFAULT 0,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_tokens_queue FOREIGN KEY (queue_id) REFERENCES service_queues(id),
    CONSTRAINT fk_tokens_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_tokens_priority_by FOREIGN KEY (priority_assigned_by) REFERENCES users(id),
    CONSTRAINT fk_tokens_cancelled_by FOREIGN KEY (cancelled_by) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE INDEX idx_tokens_queue_status ON tokens(queue_id, status);
CREATE INDEX idx_tokens_queue_position ON tokens(queue_id, queue_position);
CREATE INDEX idx_tokens_user_status ON tokens(user_id, status);
CREATE INDEX idx_tokens_status ON tokens(status);
CREATE INDEX idx_tokens_hold_expiry ON tokens(hold_expiry_time);
CREATE INDEX idx_tokens_queue_priority ON tokens(queue_id, priority, queue_position);

-- ============================================================
-- TOKEN HISTORY TABLE
-- ============================================================
CREATE TABLE token_history (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    token_id        BIGINT          NOT NULL,
    from_status     ENUM('WAITING', 'HELD', 'CALLED', 'SERVING', 'SKIPPED', 'CANCELLED', 'COMPLETED', 'EXPIRED') NULL,
    to_status       ENUM('WAITING', 'HELD', 'CALLED', 'SERVING', 'SKIPPED', 'CANCELLED', 'COMPLETED', 'EXPIRED') NOT NULL,
    action          VARCHAR(50)     NOT NULL,
    action_by       VARCHAR(20)     NOT NULL DEFAULT 'SYSTEM',
    action_by_id    BIGINT          NULL,
    details         VARCHAR(500)    NULL,
    position_before INT             NULL,
    position_after  INT             NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_token_history_token FOREIGN KEY (token_id) REFERENCES tokens(id),
    CONSTRAINT fk_token_history_action_by FOREIGN KEY (action_by_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE INDEX idx_token_history_token ON token_history(token_id);
CREATE INDEX idx_token_history_action ON token_history(action);
CREATE INDEX idx_token_history_created ON token_history(created_at);

-- ============================================================
-- NOTIFICATIONS TABLE
-- ============================================================
CREATE TABLE notifications (
    id          BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT          NOT NULL,
    token_id    BIGINT          NULL,
    queue_id    BIGINT          NULL,
    title       VARCHAR(200)    NOT NULL,
    message     TEXT            NOT NULL,
    type        ENUM('QUEUE_JOINED', 'TURN_APPROACHING', 'YOUR_TURN', 'QUEUE_CLOSED',
                     'HOLD_EXPIRING', 'TOKEN_CANCELLED', 'TOKEN_COMPLETED', 'PRIORITY_CHANGED',
                     'GENERAL') NOT NULL DEFAULT 'GENERAL',
    is_read     BOOLEAN         NOT NULL DEFAULT FALSE,
    channel     VARCHAR(30)     NOT NULL DEFAULT 'IN_APP',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_notifications_token FOREIGN KEY (token_id) REFERENCES tokens(id),
    CONSTRAINT fk_notifications_queue FOREIGN KEY (queue_id) REFERENCES service_queues(id)
) ENGINE=InnoDB;

CREATE INDEX idx_notifications_user_read ON notifications(user_id, is_read);
CREATE INDEX idx_notifications_user_created ON notifications(user_id, created_at DESC);

-- ============================================================
-- SEED DATA — Default Admin Account
-- ============================================================
-- Password: Admin@123 (BCrypt hashed)
INSERT INTO users (name, email, mobile, password, role, preferred_language)
VALUES (
    'System Admin',
    'admin@smartqueue.com',
    '9999999999',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN',
    'en'
);
