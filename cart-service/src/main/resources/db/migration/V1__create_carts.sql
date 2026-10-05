-- One cart per customer, items from one restaurant at a time.
CREATE TABLE carts (
    customer_id    UUID PRIMARY KEY,
    restaurant_id  BIGINT,
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE cart_items (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id   UUID         NOT NULL REFERENCES carts(customer_id) ON DELETE CASCADE,
    menu_item_id  BIGINT       NOT NULL,
    name          VARCHAR(120) NOT NULL,
    emoji         VARCHAR(16),
    unit_price    NUMERIC(8,2) NOT NULL,
    quantity      INT          NOT NULL CHECK (quantity > 0),
    UNIQUE (customer_id, menu_item_id)
);
