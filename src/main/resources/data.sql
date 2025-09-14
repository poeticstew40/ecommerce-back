CREATE TABLE usuarios (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    contrasena VARCHAR(255) NOT NULL,
    nombre VARCHAR(255),
    apellido VARCHAR(255)
);

CREATE TABLE pedidos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    usuario_id INT NOT NULL,
    fecha_pedido TIMESTAMP NOT NULL,
    estado VARCHAR(255),
    total DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE TABLE categorias (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL
);

CREATE TABLE productos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL,
    categoria_id INT,
    FOREIGN KEY (categoria_id) REFERENCES categorias(id)
);

CREATE TABLE items_pedidos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    pedido_id INT NOT NULL,
    producto_id INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id),
    FOREIGN KEY (producto_id) REFERENCES productos(id)
);

-- Insertar datos de prueba en la tabla usuarios
INSERT INTO usuarios (email, contrasena, nombre, apellido) VALUES
('juan.perez@example.com', 'pass123', 'Juan', 'Perez'),
('maria.lopez@example.com', 'pass456', 'Maria', 'Lopez');

-- Insertar datos de prueba en la tabla categorias
INSERT INTO categorias (nombre) VALUES
('Electrónica'),
('Hogar'),
('Libros');

-- Insertar datos de prueba en la tabla productos
INSERT INTO productos (nombre, descripcion, precio, stock, categoria_id) VALUES
('Smartphone A', 'Último modelo de smartphone con cámara avanzada.', 899.99, 50, 1),
('Auriculares Bluetooth', 'Auriculares inalámbricos con cancelación de ruido.', 129.50, 150, 1),
('Lámpara de Escritorio LED', 'Lámpara moderna y ajustable para tu escritorio.', 45.00, 200, 2),
('El Señor de los Anillos', 'Trilogía completa en tapa dura.', 75.20, 80, 3);

-- Insertar datos de prueba en la tabla pedidos
INSERT INTO pedidos (usuario_id, fecha_pedido, estado, total) VALUES
(1, '2025-09-13 10:00:00', 'enviado', 974.99),
(2, '2025-09-13 11:30:00', 'pendiente', 45.00);

-- Insertar datos de prueba en la tabla items_pedidos
INSERT INTO items_pedidos (pedido_id, producto_id, cantidad, precio_unitario) VALUES
(1, 1, 1, 899.99),
(1, 2, 1, 129.50),
(2, 3, 1, 45.00);