CREATE TABLE tour_schedule
(
    schedule_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    tour_id BIGINT NOT NULL,

    departure_date DATE NOT NULL,

    return_date DATE NOT NULL,

    available_seats INT NOT NULL DEFAULT 0,

    price DECIMAL(10,2) NOT NULL,

    CONSTRAINT fk_tour_schedule_tour
        FOREIGN KEY (tour_id)
        REFERENCES tour(tour_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT chk_available_seats
        CHECK (available_seats >= 0),

    CONSTRAINT chk_schedule_price
        CHECK (price > 0),

    CONSTRAINT chk_schedule_dates
        CHECK (return_date >= departure_date),

    INDEX idx_schedule_tour (tour_id),

    INDEX idx_departure_date (departure_date)
);