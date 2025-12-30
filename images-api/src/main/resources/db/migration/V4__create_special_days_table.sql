CREATE TABLE special_days(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,          -- Ej: "San Valentín"
    day_month INT NOT NULL,              -- Ej: 14
    month_of_year INT NOT NULL,          -- Ej: 2 (Febrero)
    theme_id BIGINT NOT NULL,            -- El tema que queremos mostrar (FK)

    -- Campos de auditoría (igual que tus otras tablas)
    created_by VARCHAR(255) DEFAULT NULL,
    created DATETIME(6) NOT NULL,
    last_modified_by VARCHAR(255) DEFAULT NULL,
    last_modified DATETIME(6) NOT NULL,

    CONSTRAINT fk_special_days_theme
        FOREIGN KEY (theme_id) REFERENCES themes(id)
        ON DELETE CASCADE
);

-- Índice para búsqueda rápida por fecha
CREATE INDEX idx_special_days_date ON special_days(day_month, month_of_year);

-- DATOS DE PRUEBA (Opcional, para que veas algo funcionando)
-- Asegúrate de tener un tema con ID 1 o cambia el ID por uno real de tu BD
-- INSERT INTO special_days (name, day_month, month_of_year, theme_id, created, last_modified)
-- VALUES ('Prueba Hoy', DAY(CURRENT_DATE), MONTH(CURRENT_DATE), 1, NOW(), NOW());