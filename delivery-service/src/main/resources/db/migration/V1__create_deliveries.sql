CREATE TABLE deliveries (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id          BIGINT        NOT NULL UNIQUE,   -- UNIQUE makes event handling idempotent
    restaurant_id     BIGINT        NOT NULL,
    customer_id       UUID          NOT NULL,
    delivery_address  VARCHAR(255)  NOT NULL,
    order_total       NUMERIC(10,2) NOT NULL,
    partner_id        UUID,
    status            VARCHAR(30)   NOT NULL,          -- AWAITING_PARTNER | ASSIGNED | PICKED_UP | DELIVERED
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE INDEX idx_deliveries_status ON deliveries(status);
CREATE INDEX idx_deliveries_partner ON deliveries(partner_id);
