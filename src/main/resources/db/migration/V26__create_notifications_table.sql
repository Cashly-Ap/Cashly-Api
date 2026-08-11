CREATE TABLE notifications (
    id          BINARY(16) PRIMARY KEY,
    user_id     BINARY(16) NOT NULL,
    type        VARCHAR(50) NOT NULL,
    title       VARCHAR(200) NOT NULL,
    message     VARCHAR(1000) NOT NULL,
    reference_type VARCHAR(50) NULL,
    reference_id   BINARY(16) NULL,
    is_read     BOOLEAN NOT NULL DEFAULT FALSE,
    read_at     TIMESTAMP NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_notifications_user_id (user_id),
    INDEX idx_notifications_user_read (user_id, is_read),
    INDEX idx_notifications_created_at (created_at),

    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id)
);
