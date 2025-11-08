# 📔 Documentación de la API - E-commerce

Este backend tiene dos entornos:

### 1. Entorno de Producción (Online)
* **URL Base:** `https://ecommerce-back-m9zg.onrender.com/ecommerce`
* **Nota:** Esta API está en un plan gratuito. Después de 15 minutos de inactividad, el servidor se "duerme". Si una petición falla o tarda mucho, esperá 30 segundos y volvé a intentarlo.

### 2. Entorno de Desarrollo (Local)
* **URL Base:** `http://localhost:8080/ecommerce`
* **Para correrlo:** Abrí la carpeta `backend` en tu IDE y ejecutá `EcommerceApplication.java`.
* **Consola H2 (para ver la DB):** `http://localhost:8080/ecommerce/h2`
    * **JDBC URL:** `jdbc:h2:mem:ecommerce`
    * **User:** `sa`
    * **Password:** `password`

---
## 📦 Productos
*Controlador de Productos*

### Obtener todos los productos
* **Método:** `GET`
* **Path:** `/productos`
* **Descripción:** Devuelve una lista de todos los productos del catálogo.
* **Ejemplo de Response Body (`200 OK`):**
    ```json
    [
      {
        "id": 1,
        "categoriaId": 1,
        "nombre": "Monitor Gamer",
        "descripcion": "Monitor curvo de 27 pulgadas",
        "precio": 150.0,
        "stock": 20,
        "categoriaNombre": "Monitores",
        "imagen": "url_imagen.png"
      },
      {
        "id": 2,
        "categoriaId": 2,
        "nombre": "Teclado Mecánico",
        "descripcion": "Teclado con switches rojos",
        "precio": 85.5,
        "stock": 50,
        "categoriaNombre": "Periféricos",
        "imagen": null
      }
    ]
    ```

### Crear un nuevo producto
* **Método:** `POST`
* **Path:** `/productos`
* **Descripción:** Crea un nuevo producto en la base de datos.
* **Ejemplo de Request Body:**
    ```json
    {
      "categoriaId": 1,
      "nombre": "Mouse Inalámbrico",
      "descripcion": "Mouse ergonómico con batería recargable",
      "precio": 45.0,
      "stock": 100,
      "imagen": "url_mouse.jpg"
    }
    ```

### Buscar productos por nombre
* **Método:** `GET`
* **Path:** `/productos/buscar`
* **Descripción:** Devuelve una lista de productos cuyo nombre contenga el término de búsqueda (no distingue mayúsculas/minúsculas).
* **Parámetro de Query:** `q` (ej: `/productos/buscar?q=teclado`)
* **Ejemplo de Response Body (`200 OK`):**
    ```json
    [
      {
        "id": 2,
        "categoriaId": 2,
        "nombre": "Teclado Mecánico",
        "descripcion": "Teclado con switches rojos",
        "precio": 85.5,
        "stock": 50,
        "categoriaNombre": "Periféricos",
        "imagen": null
      }
    ]
    ```

### Obtener productos por categoría
* **Método:** `GET`
* **Path:** `/productos/categoria/{id}`
* **Descripción:** Devuelve una lista de todos los productos que pertenecen a una categoría específica.
* **Ejemplo de Response Body (`200 OK`):**
    ```json
    [
      {
        "id": 2,
        "categoriaId": 2,
        "nombre": "Teclado Mecánico",
        "descripcion": "Teclado con switches rojos",
        "precio": 85.5,
        "stock": 50,
        "categoriaNombre": "Periféricos",
        "imagen": null
      }
    ]
    ```

### Obtener un producto por ID
* **Método:** `GET`
* **Path:** `/productos/{id}`
* **Descripción:** Recupera los detalles de un producto específico.

### Actualizar un producto (parcial)
* **Método:** `PATCH`
* **Path:** `/productos/{id}`
* **Descripción:** Actualiza uno o más campos de un producto existente.
* **Ejemplo de Request Body:**
    ```json
    {
      "precio": 90.0,
      "stock": 45
    }
    ```

### Eliminar un producto
* **Método:** `DELETE`
* **Path:** `/productos/{id}`
* **Descripción:** Elimina un producto de la base de datos.
* **Response Body:** `204 No Content`

---
## 🗂️ Categorías
*Controlador de Categorías*

### Obtener todas las categorías
* **Método:** `GET`
* **Path:** `/categorias`
* **Ejemplo de Response Body (`200 OK`):**
    ```json
    [
      {
        "id": 1,
        "nombre": "Monitores"
      },
      {
        "id": 2,
        "nombre": "Periféricos"
      }
    ]
    ```

### Crear una nueva categoría
* **Método:** `POST`
* **Path:** `/categorias`
* **Ejemplo de Request Body:**
    ```json
    {
      "nombre": "Componentes de PC"
    }
    ```

### Obtener una categoría por ID
* **Método:** `GET`
* **Path:** `/categorias/{id}`

### Actualizar una categoría
* **Método:** `PATCH`
* **Path:** `/categorias/{id}`
* **Ejemplo de Request Body:**
    ```json
    {
      "nombre": "Monitores y Pantallas"
    }
    ```

### Eliminar una categoría
* **Método:** `DELETE`
* **Path:** `/categorias/{id}`
* **Response Body:** `204 No Content`

---
## 👤 Usuarios
*Controlador de Usuarios*

### Obtener todos los usuarios
* **Método:** `GET`
* **Path:** `/usuarios`
* **Ejemplo de Response Body (`200 OK`):**
    ```json
    [
      {
        "dni": 12345678,
        "email": "juan.perez@mail.com",
        "nombre": "Juan",
        "apellido": "Perez"
      },
      {
        "dni": 87654321,
        "email": "maria.gomez@mail.com",
        "nombre": "Maria",
        "apellido": "Gomez"
      }
    ]
    ```

### Crear un nuevo usuario
* **Método:** `POST`
* **Path:** `/usuarios`
* **Ejemplo de Request Body:**
    ```json
    {
      "dni": 12345678,
      "email": "juan.perez@mail.com",
      "password": "unaClaveSegura123",
      "nombre": "Juan",
      "apellido": "Perez"
    }
    ```

### Obtener un usuario por DNI
* **Método:** `GET`
* **Path:** `/usuarios/{dni}`

### Actualizar un usuario (parcial)
* **Método:** `PATCH`
* **Path:** `/usuarios/{dni}`
* **Ejemplo de Request Body:**
    ```json
    {
      "nombre": "Juan Carlos",
      "email": "jc.perez@nuevo-mail.com"
    }
    ```

### Eliminar un usuario
* **Método:** `DELETE`
* **Path:** `/usuarios/{dni}`
* **Response Body:** `204 No Content`

---
## 🧾 Pedidos
*Controlador de Pedidos*

### Crear un nuevo pedido
* **Método:** `POST`
* **Path:** `/pedidos`
* **Descripción:** Crea un nuevo pedido. El `total` se calcula automáticamente en el backend.
* **Ejemplo de Request Body:**
    ```json
    {
      "usuarioDni": 12345678,
      "items": [
        {
          "idProducto": 1,
          "cantidad": 2
        },
        {
          "idProducto": 2,
          "cantidad": 1
        }
      ]
    }
    ```

### Obtener un pedido por ID
* **Método:** `GET`
* **Path:** `/pedidos/{id}`
* **Ejemplo de Response Body (`200 OK`):**
    ```json
    {
      "id": 1,
      "estado": "Pendiente",
      "total": 385.5,
      "fechaPedido": "2025-11-07T14:30:00",
      "items": [
        {
          "cantidad": 2,
          "precioUnitario": 150.0,
          "nombreProducto": "Monitor Gamer",
          "descripcionProducto": "Monitor curvo de 27 pulgadas",
          "idProducto": 1
        },
        {
          "cantidad": 1,
          "precioUnitario": 85.5,
          "nombreProducto": "Teclado Mecánico",
          "descripcionProducto": "Teclado con switches rojos",
          "idProducto": 2
        }
      ],
      "usuarioDni": 12345678
    }
    ```

### Actualizar el estado de un pedido
* **Método:** `PATCH`
* **Path:** `/pedidos/{id}`
* **Descripción:** Actualiza el estado de un pedido (ej. "CANCELADO" o "ENVIADO").
* **Ejemplo de Request Body:**
    ```json
    {
      "estado": "CANCELADO"
    }
    ```

### Eliminar un pedido
* **Método:** `DELETE`
* **Path:** `/pedidos/{id}`
* **Descripción:** Elimina un pedido y sus items asociados.
* **Response Body:** `204 No Content`

### Obtener pedidos por DNI de usuario
* **Método:** `GET`
* **Path:** `/pedidos/usuario/{dni}`
* **Descripción:** Devuelve una lista con el historial de todos los pedidos de un usuario.
