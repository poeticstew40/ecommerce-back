-- ==================================================================
-- 0. LIMPIEZA TOTAL (Para evitar errores de ids o duplicados)
-- ==================================================================
DELETE FROM items_pedidos;
DELETE FROM pedidos;
DELETE FROM productos;
DELETE FROM categorias;
DELETE FROM tiendas;
DELETE FROM usuarios WHERE email = 'vendedor@techhaven.com';

-- ==================================================================
-- 1. CREAR USUARIO VENDEDOR
-- ==================================================================
INSERT INTO usuarios (dni, email, nombre, apellido, password, rol) 
VALUES (33333333, 'vendedor@techhaven.com', 'Martin', 'Tech', '$2a$10$domp.e3/gZc.yR/i.u/a..4H6/6.1/5.6/7.8/9.0', 'VENDEDOR');

-- ==================================================================
-- 2. CREAR LA TIENDA "TechHaven"
-- ==================================================================
INSERT INTO tiendas (nombre_url, nombre_fantasia, descripcion, logo, vendedor_dni) 
VALUES ('tech-haven', 'TechHaven Oficial', 'Líderes en tecnología y hardware de alto rendimiento.', 'https://http2.mlstatic.com/frontend-assets/ui-navigation/5.19.5/mercadolibre/logo__large_plus.png', 33333333);

-- Guardamos el ID de la tienda para usarlo abajo
SET @tienda_id = (SELECT id FROM tiendas WHERE nombre_url = 'tech-haven');

-- ==================================================================
-- 3. CREAR CATEGORÍAS
-- ==================================================================
INSERT INTO categorias (nombre, tienda_id) VALUES ('Celulares', @tienda_id);
INSERT INTO categorias (nombre, tienda_id) VALUES ('Notebooks', @tienda_id);
INSERT INTO categorias (nombre, tienda_id) VALUES ('Consolas', @tienda_id);
INSERT INTO categorias (nombre, tienda_id) VALUES ('Periféricos', @tienda_id);

-- Guardamos IDs de categorías
SET @cat_celu = (SELECT id FROM categorias WHERE nombre = 'Celulares' AND tienda_id = @tienda_id);
SET @cat_note = (SELECT id FROM categorias WHERE nombre = 'Notebooks' AND tienda_id = @tienda_id);
SET @cat_cons = (SELECT id FROM categorias WHERE nombre = 'Consolas' AND tienda_id = @tienda_id);
SET @cat_peri = (SELECT id FROM categorias WHERE nombre = 'Periféricos' AND tienda_id = @tienda_id);

-- ==================================================================
-- 4. CREAR PRODUCTOS (Fotos estilo E-commerce)
-- ==================================================================

-- --- CELULARES ---
INSERT INTO productos (nombre, descripcion, precio, stock, imagen, categoria_id, tienda_id) 
VALUES ('iPhone 15 Pro Max 256GB', 'Titanio Natural. El chip A17 Pro cambia las reglas del juego. Cámara de 48MP.', 1800.00, 10, 
'https://http2.mlstatic.com/D_NQ_NP_799254-MLA71782868848_092023-O.webp', 
@cat_celu, @tienda_id);

INSERT INTO productos (nombre, descripcion, precio, stock, imagen, categoria_id, tienda_id) 
VALUES ('Samsung Galaxy S24 Ultra', 'Titanium Grey 512GB. Galaxy AI ya llegó. Note Assist organiza tus notas.', 1650.00, 15, 
'https://http2.mlstatic.com/D_NQ_NP_927676-MLA74358536819_022024-O.webp', 
@cat_celu, @tienda_id);

-- --- NOTEBOOKS ---
INSERT INTO productos (nombre, descripcion, precio, stock, imagen, categoria_id, tienda_id) 
VALUES ('MacBook Air M2 13.6"', 'Color Medianoche. Chip M2 de Apple. 8GB RAM, 256GB SSD.', 1450.00, 8, 
'https://http2.mlstatic.com/D_NQ_NP_641953-MLA51363506027_092022-O.webp', 
@cat_note, @tienda_id);

INSERT INTO productos (nombre, descripcion, precio, stock, imagen, categoria_id, tienda_id) 
VALUES ('Notebook Gamer Lenovo Legion', 'Core i7, RTX 4060, 16GB RAM. Pantalla 165Hz para eSports.', 1900.00, 5, 
'https://http2.mlstatic.com/D_NQ_NP_892662-MLA75866846397_042024-O.webp', 
@cat_note, @tienda_id);

-- --- CONSOLAS ---
INSERT INTO productos (nombre, descripcion, precio, stock, imagen, categoria_id, tienda_id) 
VALUES ('Sony PlayStation 5 Slim', 'Edición Standard con lector de disco. 1TB SSD. DualSense incluido.', 850.00, 20, 
'https://http2.mlstatic.com/D_NQ_NP_834238-MLA74309378525_012024-O.webp', 
@cat_cons, @tienda_id);

INSERT INTO productos (nombre, descripcion, precio, stock, imagen, categoria_id, tienda_id) 
VALUES ('Nintendo Switch OLED', 'Pantalla de 7 pulgadas colores vibrantes. Joy-Con blanco.', 450.00, 30, 
'https://http2.mlstatic.com/D_NQ_NP_636305-MLA47781643006_102021-O.webp', 
@cat_cons, @tienda_id);

-- --- PERIFÉRICOS ---
INSERT INTO productos (nombre, descripcion, precio, stock, imagen, categoria_id, tienda_id) 
VALUES ('Joystick PS5 DualSense', 'Control inalámbrico Midnight Black. Retroalimentación háptica.', 90.00, 50, 
'https://http2.mlstatic.com/D_NQ_NP_669080-MLA46412057429_062021-O.webp', 
@cat_peri, @tienda_id);

INSERT INTO productos (nombre, descripcion, precio, stock, imagen, categoria_id, tienda_id) 
VALUES ('Mouse Logitech G502 Hero', 'Sensor HERO 25K, 11 botones programables, RGB LIGHTSYNC.', 60.00, 40, 
'https://http2.mlstatic.com/D_NQ_NP_832980-MLA44314617562_122020-O.webp', 
@cat_peri, @tienda_id);