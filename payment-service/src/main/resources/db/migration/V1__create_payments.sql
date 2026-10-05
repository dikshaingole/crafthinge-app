CREATE TABLE payments (
    id              UUID PRIMARY KEY,
    order_id        BIGINT        NOT NULL,
    customer_id     UUID          NOT NULL,
    amount          NUMERIC(10,2) NOT NULL CHECK (amount >= 0),
    currency        VARCHAR(3)    NOT NULL DEFAULT 'USD',
    status          VARCHAR(20)   NOT NULL,         -- SUCCEEDED | FAILED
    provider        VARCHAR(30)   NOT NULL,
    provider_ref    VARCHAR(100),
    card_last4      VARCHAR(4),                     -- never the full card number
    failure_reason  VARCHAR(200),
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now()
);
-- Idempotency: an order can be paid successfully only once, even if the request is retried.
CREATE UNIQUE INDEX uq_payments_order_succeeded ON payments(order_id) WHERE status = 'SUCCEEDED';
