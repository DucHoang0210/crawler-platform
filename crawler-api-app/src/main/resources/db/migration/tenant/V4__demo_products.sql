CREATE TABLE demo_products
(
    id BIGSERIAL PRIMARY KEY,

    public_id UUID NOT NULL UNIQUE,

    name VARCHAR(255) NOT NULL,

    regular_price NUMERIC(19, 2) NOT NULL,

    current_price NUMERIC(19, 2) NOT NULL,

    in_stock BOOLEAN NOT NULL DEFAULT TRUE,

    promotion_text VARCHAR(500),

    created_at TIMESTAMP NOT NULL
                              DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL
                              DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_demo_regular_price
        CHECK (regular_price >= 0),

    CONSTRAINT chk_demo_current_price
        CHECK (current_price >= 0)
);


CREATE INDEX idx_demo_products_public_id
    ON demo_products(public_id);