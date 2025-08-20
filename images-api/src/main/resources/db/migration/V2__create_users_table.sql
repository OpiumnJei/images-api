CREATE TABLE users(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL, -- Columna para guardar "ADMIN" o "CLIENT"
    created_by VARCHAR(255) DEFAULT NULL,
    created DATETIME(6) NOT NULL,
    last_modified_by VARCHAR(255) DEFAULT NULL,
    last_modified DATETIME(6) NOT NULL
);