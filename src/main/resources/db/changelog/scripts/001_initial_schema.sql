CREATE TABLE IF NOT EXISTS usrloginfo (
       id SERIAL PRIMARY KEY,
       username VARCHAR(50) NOT NULL UNIQUE,
       first_name VARCHAR(100) NOT NULL,
       last_name  VARCHAR(100) NOT NULL,
       password VARCHAR(500) NOT NULL,
       email VARCHAR(100) NOT NULL UNIQUE,
       mfa_secret VARCHAR(32) DEFAULT NULL,
       two_factor_enabled boolean DEFAULT FALSE,
       auth_strategy VARCHAR DEFAULT 'ONE_STEP_AUTH',
       two_factor_message_strategy VARCHAR DEFAULT 'NONE',
       is_account_non_expired BOOLEAN DEFAULT TRUE,
       is_account_non_locked BOOLEAN DEFAULT TRUE,
       is_credentials_non_expired BOOLEAN DEFAULT TRUE,
       is_enabled BOOLEAN DEFAULT TRUE,
       created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
       updated_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE IF NOT EXISTS roles (
     id BIGSERIAL NOT NULL PRIMARY KEY,
     role_name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS user_roles (
     user_id                     BIGSERIAL NOT NULL,
     role_id                     BIGSERIAL NOT NULL,
     PRIMARY KEY (user_id, role_id),
     CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES usrloginfo(id) ON DELETE CASCADE,
     CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);
