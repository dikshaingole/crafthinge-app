CREATE TABLE orders (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id       UUID          NOT NULL,
    restaurant_id     BIGINT        NOT NULL,
    restaurant_name   VARCHAR(120)  NOT NULL,
    status            VARCHAR(30)   NOT NULL,
    subtotal          NUMERIC(10,2) NOT NULL,
    delivery_fee      NUMERIC(6,2)  NOT NULL,
    total             NUMERIC(10,2) NOT NULL,
    delivery_address  VARCHAR(255)  NOT NULL,
    payment_id        UUID,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    version           BIGINT        NOT NULL DEFAULT 0     -- optimistic locking for concurrent status updates
);
CREATE INDEX idx_orders_customer ON orders(customer_id, created_at DESC);
CREATE INDEX idx_orders_restaurant ON orders(restaurant_id, created_at DESC);

CREATE TABLE order_items (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id      BIGINT        NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    menu_item_id  BIGINT        NOT NULL,
    name          VARCHAR(120)  NOT NULL,
    emoji         VARCHAR(16),
    unit_price    NUMERIC(8,2)  NOT NULL,
    quantity      INT           NOT NULL
);

CREATE TABLE order_status_history (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id    BIGINT       NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    status      VARCHAR(30)  NOT NULL,
    changed_by  UUID,
    changed_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
