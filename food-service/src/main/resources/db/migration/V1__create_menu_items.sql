CREATE TABLE menu_items (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    restaurant_id  BIGINT        NOT NULL,   -- owned by restaurant-service (no cross-DB foreign key)
    name           VARCHAR(120)  NOT NULL,
    description    VARCHAR(500),
    category       VARCHAR(60)   NOT NULL,
    price          NUMERIC(8,2)  NOT NULL CHECK (price >= 0),
    emoji          VARCHAR(16),
    vegetarian     BOOLEAN       NOT NULL DEFAULT FALSE,
    spicy          BOOLEAN       NOT NULL DEFAULT FALSE,
    available      BOOLEAN       NOT NULL DEFAULT TRUE,
    featured       BOOLEAN       NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_menu_items_restaurant ON menu_items(restaurant_id);
