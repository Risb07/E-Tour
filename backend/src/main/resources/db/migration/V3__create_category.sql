CREATE TABLE category
(
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    parent_category_id BIGINT,

    category_name VARCHAR(100) NOT NULL,

    description TEXT,

    image_url VARCHAR(255),

    status BOOLEAN NOT NULL DEFAULT TRUE,

    category_code VARCHAR(10),

    is_featured CHAR(1),

    CONSTRAINT fk_category_parent
        FOREIGN KEY (parent_category_id)
        REFERENCES category(category_id)
        ON UPDATE CASCADE
        ON DELETE SET NULL,

    CONSTRAINT chk_category_code
        CHECK (category_code IN ('DOM', 'ADV', 'INT')),

    CONSTRAINT chk_is_featured
        CHECK (is_featured IN ('Y', 'N')),

    INDEX idx_category_parent(parent_category_id),
    INDEX idx_category_name(category_name),
    INDEX idx_category_code(category_code)
);