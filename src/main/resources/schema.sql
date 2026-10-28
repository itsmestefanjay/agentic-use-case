CREATE TABLE customers (
    id                VARCHAR(20) PRIMARY KEY,
    name              VARCHAR(100) NOT NULL,
    email             VARCHAR(100) NOT NULL,
    phone             VARCHAR(30),
    preferred_channel VARCHAR(10)  NOT NULL,
    language          VARCHAR(5)   NOT NULL,
    tier              VARCHAR(10)  NOT NULL
);

CREATE TABLE orders (
    id                VARCHAR(20) PRIMARY KEY,
    customer_id       VARCHAR(20)   NOT NULL REFERENCES customers (id),
    order_date        DATE          NOT NULL,
    status            VARCHAR(20)   NOT NULL,
    status_updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    total             DECIMAL(10, 2) NOT NULL,
    currency          VARCHAR(3)    NOT NULL,
    shipping_city     VARCHAR(60)   NOT NULL,
    shipping_country  VARCHAR(2)    NOT NULL
);

CREATE TABLE order_items (
    order_id   VARCHAR(20)    NOT NULL REFERENCES orders (id),
    sku        VARCHAR(20)    NOT NULL,
    name       VARCHAR(100)   NOT NULL,
    quantity   INT            NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL
);

CREATE TABLE shipments (
    order_id           VARCHAR(20) PRIMARY KEY REFERENCES orders (id),
    carrier            VARCHAR(20) NOT NULL,
    tracking_number    VARCHAR(40) NOT NULL,
    shipped_at         TIMESTAMP WITH TIME ZONE NOT NULL,
    estimated_delivery TIMESTAMP WITH TIME ZONE NOT NULL,
    delivered_at       TIMESTAMP WITH TIME ZONE
);

CREATE TABLE tracking_events (
    order_id    VARCHAR(20)  NOT NULL REFERENCES shipments (order_id),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    location    VARCHAR(60)  NOT NULL,
    description VARCHAR(200) NOT NULL
);

CREATE TABLE notifications (
    id           VARCHAR(40) PRIMARY KEY,
    customer_id  VARCHAR(20),
    channel      VARCHAR(10)   NOT NULL,
    recipient    VARCHAR(100),
    order_status VARCHAR(20),
    message      VARCHAR(4000) NOT NULL,
    sent_at      TIMESTAMP WITH TIME ZONE NOT NULL
);
