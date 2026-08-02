CREATE TABLE tour_category
(
    tour_id BIGINT NOT NULL,

    category_id BIGINT NOT NULL,

    PRIMARY KEY (tour_id, category_id),

    CONSTRAINT fk_tour_category_tour
        FOREIGN KEY (tour_id)
        REFERENCES tour(tour_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_tour_category_category
        FOREIGN KEY (category_id)
        REFERENCES category(category_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    INDEX idx_tour_category_tour (tour_id),

    INDEX idx_tour_category_category (category_id)
);