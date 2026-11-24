-- Run once in the NeonDB SQL editor. Banks and categories are seeded by you.

CREATE TABLE banks (
    bank_id    BIGSERIAL PRIMARY KEY,
    bank_name  VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE transaction_categories (
    category_id BIGSERIAL PRIMARY KEY,
    category    VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
    user_id        BIGSERIAL PRIMARY KEY,
    mobile_number  VARCHAR(20) NOT NULL UNIQUE,
    name           VARCHAR(100) NOT NULL,
    registered_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE bank_users (
    bank_user_id     BIGSERIAL PRIMARY KEY,
    user_id          BIGINT NOT NULL REFERENCES users(user_id),
    bank_id          BIGINT NOT NULL REFERENCES banks(bank_id),
    opening_balance  NUMERIC(15,2) NOT NULL DEFAULT 0,
    is_active        BOOLEAN NOT NULL DEFAULT TRUE,   -- false = bank removed by user (soft delete)
    linked_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, bank_id)
);

CREATE TABLE transactions (
    transaction_id           BIGSERIAL PRIMARY KEY,
    bank_user_id             BIGINT NOT NULL REFERENCES bank_users(bank_user_id),
    transaction_type         VARCHAR(6) NOT NULL CHECK (transaction_type IN ('CREDIT', 'DEBIT')),
    transaction_amount       NUMERIC(15,2) NOT NULL CHECK (transaction_amount > 0),
    transaction_category_id  BIGINT NOT NULL REFERENCES transaction_categories(category_id),
    transaction_details      VARCHAR(500),
    transaction_date         DATE NOT NULL,                      -- date chosen by the user
    created_at               TIMESTAMPTZ NOT NULL DEFAULT now(), -- when the row was created
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT now()  -- when the row was last modified
);

CREATE INDEX idx_transactions_bank_user ON transactions(bank_user_id);
CREATE INDEX idx_bank_users_user ON bank_users(user_id);

-- Example seed (edit as you like):
-- INSERT INTO banks (bank_name) VALUES ('HDFC Bank'), ('SBI'), ('ICICI Bank'), ('Axis Bank');
-- INSERT INTO transaction_categories (category) VALUES ('Shopping'), ('Travelling'), ('Bills'), ('Food'), ('Salary'), ('Other');
