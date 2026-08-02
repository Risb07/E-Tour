CREATE TABLE review
(
    review_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    tour_id BIGINT NOT NULL,

    customer_id BIGINT NOT NULL,

    rating INT NOT NULL,

    comment TEXT NOT NULL,

    CONSTRAINT fk_review_tour
        FOREIGN KEY (tour_id)
        REFERENCES tour(tour_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_review_customer
        FOREIGN KEY (customer_id)
        REFERENCES customer(customer_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT chk_review_rating
        CHECK (rating BETWEEN 1 AND 5),

    INDEX idx_review_tour (tour_id),

    INDEX idx_review_customer (customer_id)
);