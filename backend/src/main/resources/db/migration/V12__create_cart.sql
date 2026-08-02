CREATE TABLE cart
(
    cart_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    user_id BIGINT NOT NULL,

    tour_id BIGINT NOT NULL,

    quantity INT NOT NULL,

    CONSTRAINT fk_cart_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_cart_tour
        FOREIGN KEY (tour_id)
        REFERENCES tour(tour_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT chk_cart_quantity
        CHECK (quantity >= 1),

    INDEX idx_cart_user (user_id),

    INDEX idx_cart_tour (tour_id),

    UNIQUE KEY uk_cart_user_tour (user_id, tour_id)
);