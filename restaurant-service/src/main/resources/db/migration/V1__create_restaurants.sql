CREATE TABLE restaurants (
    id                     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    owner_id               UUID          NOT NULL,
    name                   VARCHAR(120)  NOT NULL,
    cuisine                VARCHAR(40)   NOT NULL,
    description            VARCHAR(500),
    address                VARCHAR(255)  NOT NULL,
    city                   VARCHAR(80)   NOT NULL,
    emoji                  VARCHAR(16),
    rating                 NUMERIC(2,1)  NOT NULL DEFAULT 4.5,
    delivery_time_minutes  INT           NOT NULL DEFAULT 30,
    delivery_fee           NUMERIC(6,2)  NOT NULL DEFAULT 2.99,
    is_open                BOOLEAN       NOT NULL DEFAULT TRUE,
    featured               BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at             TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE INDEX idx_restaurants_owner ON restaurants(owner_id);
CREATE INDEX idx_restaurants_cuisine ON restaurants(cuisine);
