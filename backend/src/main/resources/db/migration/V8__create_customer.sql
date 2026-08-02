CREATE TABLE customer
(
    customer_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    full_name VARCHAR(255) NOT NULL,

    email VARCHAR(255) NOT NULL UNIQUE,

    phone VARCHAR(10) NOT NULL UNIQUE,

    CONSTRAINT chk_customer_phone
        CHECK (phone REGEXP '^[0-9]{10}$'),

    INDEX idx_customer_email (email),

    INDEX idx_customer_phone (phone)
);