-- Borrar tablas si existen (importante para tests repetidos)
DROP TABLE IF EXISTS fish_captures;
DROP TABLE IF EXISTS users;

-- Crear tabla de usuarios
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255)
);

-- Crear tabla de capturas de peces
CREATE TABLE fish_captures (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    capture_date DATE NOT NULL,
    created_at TIMESTAMP,
    fish_type VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    weight FLOAT NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

---- Datos de ejemplo para usuarios
--INSERT INTO users (username, full_name, email) VALUES
--('jperez', 'Juan Pérez', 'jperez@ejemplo.com'),
--('amartin', 'Ana Martín', 'amartin@ejemplo.com');
--
---- Datos de ejemplo para capturas de peces
--INSERT INTO fish_captures (capture_date, created_at, fish_type, location, weight, user_id) VALUES
--('2025-09-25', '2025-09-25 10:00:00', 'Trucha', 'Río X', 2.5, 1),
--('2025-09-26', '2025-09-26 12:30:00', 'Lucioperca', 'Lago Y', 1.8, 2);

