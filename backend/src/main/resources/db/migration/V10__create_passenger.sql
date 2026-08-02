CREATE TABLE passenger
(
    passenger_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    booking_id BIGINT NOT NULL,

    full_name VARCHAR(150) NOT NULL,

    gender VARCHAR(10),

    dob DATE,

    nationality VARCHAR(2),

    id_proof_type VARCHAR(100),

    id_proof_number VARCHAR(50) NOT NULL UNIQUE,

    CONSTRAINT fk_passenger_booking
        FOREIGN KEY (booking_id)
        REFERENCES booking(booking_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    INDEX idx_passenger_booking (booking_id),

    INDEX idx_passenger_id_proof (id_proof_number)
);