CREATE TABLE tour
(
    tour_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    title VARCHAR(200) NOT NULL,

    description TEXT,

    duration_days INT NOT NULL,

    base_price DECIMAL(10,2) NOT NULL,

    tour_code ENUM('ADV', 'INT', 'DEV') NOT NULL,

    status ENUM('ACTIVE', 'INACTIVE', 'DRAFT') NOT NULL DEFAULT 'DRAFT',

    CONSTRAINT chk_duration_days
        CHECK (duration_days >= 1),

    CONSTRAINT chk_base_price
        CHECK (base_price > 0),

    INDEX idx_tour_code (tour_code),
    INDEX idx_tour_status (status),
    INDEX idx_tour_title (title)
);