-- Creación de la tabla de Categorías
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    created_by VARCHAR(255) DEFAULT NULL,
    created DATETIME(6) NOT NULL,
    last_modified_by VARCHAR(255) DEFAULT NULL,
    last_modified DATETIME(6) NOT NULL
);

-- Creación de la tabla de Temáticas
CREATE TABLE themes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    category_id BIGINT NOT NULL,
    created_by VARCHAR(255) DEFAULT NULL,
    created DATETIME(6) NOT NULL,
    last_modified_by VARCHAR(255) DEFAULT NULL,
    last_modified DATETIME(6) NOT NULL, -- <<< COMA AÑADIDA
    CONSTRAINT fk_themes_category
        FOREIGN KEY (category_id) REFERENCES categories(id)
        ON DELETE CASCADE
);

-- Creación de la tabla de Imágenes
CREATE TABLE images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    url TEXT NOT NULL,
    theme_id BIGINT NOT NULL,
    created_by VARCHAR(255) DEFAULT NULL,
    created DATETIME(6) NOT NULL,
    last_modified_by VARCHAR(255) DEFAULT NULL,
    last_modified DATETIME(6) NOT NULL, -- <<< COMA AÑADIDA
    CONSTRAINT fk_images_theme
        FOREIGN KEY (theme_id) REFERENCES themes(id)
        ON DELETE CASCADE
);

-- Creación de índices para mejorar el rendimiento
CREATE INDEX idx_themes_category_id ON themes(category_id);
CREATE INDEX idx_images_theme_id ON images(theme_id);