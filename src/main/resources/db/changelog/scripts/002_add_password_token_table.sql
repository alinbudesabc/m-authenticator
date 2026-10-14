CREATE TABLE IF NOT EXISTS reset_password_token(
    id SERIAL PRIMARY KEY,
    token VARCHAR(500),
    user_id BIGINT NOT NULL,
    expiration_time TIMESTAMP
);