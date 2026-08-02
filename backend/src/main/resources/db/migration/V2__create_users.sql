CREATE TABLE users
(
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    role_id BIGINT NOT NULL,

    first_name VARCHAR(100) NOT NULL,

    last_name VARCHAR(100),

    email VARCHAR(150) NOT NULL UNIQUE,

    password_hash VARCHAR(255) NOT NULL,

    phone VARCHAR(20),

    preferred_language VARCHAR(10) DEFAULT 'en',

    status BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_users_role
        FOREIGN KEY (role_id)
        REFERENCES roles(role_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    INDEX idx_users_role(role_id),
    INDEX idx_users_email(email)
);