CREATE TABLE TOURCOST
(
    cost_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    tour_id BIGINT NOT NULL,

    base_price DECIMAL(12,2) NOT NULL,

    single_person_cost DECIMAL(12,2),

    extra_person_cost DECIMAL(12,2),

    child_with_bed_cost DECIMAL(12,2),

    child_without_bed_cost DECIMAL(12,2),

    valid_from DATE NOT NULL,

    valid_to DATE NOT NULL,

    status INT NOT NULL DEFAULT 1,

    CONSTRAINT fk_tour_cost_tour
        FOREIGN KEY (tour_id)
        REFERENCES tour(tour_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT chk_tour_cost_dates
        CHECK (valid_to >= valid_from),

    INDEX idx_tour_cost_tour (tour_id),

    INDEX idx_tour_cost_valid_from (valid_from),

    INDEX idx_tour_cost_valid_to (valid_to)
);