CREATE TABLE itinerary
(
    itinerary_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    tour_id BIGINT NOT NULL,

    day_number INT NOT NULL,

    title VARCHAR(255),

    description TEXT,

    CONSTRAINT fk_itinerary_tour
        FOREIGN KEY (tour_id)
        REFERENCES tour(tour_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT chk_day_number
        CHECK (day_number >= 1),

    INDEX idx_itinerary_tour (tour_id),

    INDEX idx_itinerary_day (day_number),

    UNIQUE KEY uk_itinerary_tour_day (tour_id, day_number)
);