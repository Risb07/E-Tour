CREATE TABLE booking
(
    booking_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    customer_id BIGINT NOT NULL,

    schedule_id BIGINT NOT NULL,

    booking_date DATE NOT NULL,

    total_amount DECIMAL(12,2) NOT NULL,

    booking_status VARCHAR(255) NOT NULL DEFAULT 'PENDING',

    CONSTRAINT fk_booking_customer
        FOREIGN KEY (customer_id)
        REFERENCES customer(customer_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_booking_schedule
        FOREIGN KEY (schedule_id)
        REFERENCES tour_schedule(schedule_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT chk_total_amount
        CHECK (total_amount >= 0),

    INDEX idx_booking_customer (customer_id),

    INDEX idx_booking_schedule (schedule_id),

    INDEX idx_booking_date (booking_date),

    INDEX idx_booking_status (booking_status)
);