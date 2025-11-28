## Documentación de la API - E-commerce Multitienda

---

### Entornos y Documentación

* **URL Base de la API:** https://ecommerce-back-2uxy.onrender.com
* **Documentación Interactiva (Swagger UI):** https://ecommerce-back-2uxy.onrender.com/swagger-ui/index.html
    * **Nota para Frontend:** Esta URL proporciona una interfaz gráfica para probar y entender el funcionamiento de cada método (`GET`, `POST`, etc.) antes de la implementación en el código del frontend.

---

### 1. Autenticación y Seguridad (JWT)

La mayoría de los *endpoints* están protegidos y requieren un **JSON Web Token (JWT)** para la autenticación.

1.  **Registro o Login:** Utiliza el *endpoint* `/api/auth/register` (creación) o `/api/auth/login` (inicio de sesión) en el **Auth Controller**.
2.  **Obtención del Token:** El *token* JWT se devuelve en la respuesta JSON.
3.  **Autorización:** El *token* debe incluirse en el *header* de cada solicitud protegida, usando el formato: **`Bearer <token>`**.
    * Si se recibe un error **403 Forbidden**, se debe verificar la validez del token y la correcta inclusión del prefijo `Bearer `.

---

### 2. Arquitectura Multitienda (Slug)

El sistema utiliza un identificador único de tienda (**slug** o `nombreTienda`) en la URL para acceder a recursos específicos de esa tienda.

* **Patrón de URL:** `/api/tiendas/{nombreTienda}/...`
* **Restricción:** El acceso a recursos de una tienda con el slug de otra resultará en un error de seguridad. Es crucial usar el `nombreUrl` correcto.

---

### 3. Gestión de Imágenes (Multipart File)

Los *endpoints* de creación (`POST`) y actualización (`PATCH`) que manejan imágenes (`Productos` y `Tiendas`) consumen el tipo de contenido **`multipart/form-data`**.

* **Estructura de la Petición:**
    * **`file`:** Contiene la imagen como archivo binario.
    * **`producto`** (o **`tienda`**): Contiene los datos no binarios en formato **`application/json`**.

---

### 4. Flujo de Compra

1.  **Carrito:** Agregar ítems mediante `POST` a `/api/tiendas/{nombre}/carrito/agregar`.
2.  **Checkout (Crear Pedido):** Enviar `POST` a `/api/tiendas/{nombre}/pedidos`. Si la lista de ítems en el *Request Body* es vacía (`[]`), el sistema procesa automáticamente el contenido del carrito del usuario.
3.  **Pago (Mercado Pago):** Usar el ID del pedido generado para llamar a `POST` a `/api/pagos/crear/{id}`. Esto devolverá la URL de Mercado Pago.

---

## Endpoints

### Usuarios

| Método | Path | Descripción | Request Body | Response Body |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Crea un nuevo usuario y devuelve el token JWT. | `RegisterRequest` | `AuthResponse` |
| `POST` | `/api/auth/login` | Inicia sesión y devuelve el token JWT. | `AuthRequest` | `AuthResponse` |
| `GET` | `/api/usuarios` | Obtiene la lista de todos los usuarios. | *(Ninguno)* | Lista de `UsuariosResponse` |
| `GET` | `/api/usuarios/{dni}` | Obtiene un usuario por su DNI. | *(Ninguno)* | `UsuariosResponse` |
| `PATCH` | `/api/usuarios/{dni}` | Actualiza parcialmente un usuario. | `UsuariosRequest` | `UsuariosResponse` |
| `DELETE` | `/api/usuarios/{dni}` | Elimina un usuario por su DNI. | *(Ninguno)* | `200 OK` |
| `POST` | `/api/usuarios/direcciones` | Agrega una nueva dirección de envío al usuario. | `DireccionRequest` | `DireccionResponse` |
| `GET` | `/api/usuarios/direcciones/{dni}` | Lista las direcciones de un usuario por DNI. | *(Ninguno)* | Lista de `DireccionResponse` |
| `DELETE` | `/api/usuarios/direcciones/{id}` | Elimina una dirección por ID. | *(Ninguno)* | `200 OK` |
| `GET` | `/api/auth/verify` | Verifica la cuenta de usuario mediante un código. | *(Ninguno)* | `string` |

### Tiendas

| Método | Path | Descripción | Request Body | Response Body |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/tiendas` | Crea una nueva tienda (Multipart File). | `tienda` (JSON) y `file` (Binary) | `TiendaResponse` |
| `GET` | `/api/tiendas/{nombreUrl}` | Obtiene una tienda por su slug. | *(Ninguno)* | `TiendaResponse` |
| `PATCH` | `/api/tiendas/{nombreUrl}` | Actualiza una tienda (Multipart File). | `tienda` (JSON) y `file` (Binary) | `TiendaResponse` |

### Productos

| Método | Path | Descripción | Parámetros | Response Body |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/tiendas/{nombreTienda}/productos` | Obtiene todos los productos de una tienda. | `sort` (Query Opcional) | Lista de `ProductosResponse` |
| `POST` | `/api/tiendas/{nombreTienda}/productos` | Crea un nuevo producto (Multipart File). | *(Path)* | `ProductosResponse` |
| `GET` | `/api/tiendas/{nombreTienda}/productos/{id}` | Obtiene un producto por ID. | *(Path)* | `ProductosResponse` |
| `PATCH` | `/api/tiendas/{nombreTienda}/productos/{id}` | Actualiza parcialmente un producto. | *(Path)* | `ProductosResponse` |
| `DELETE` | `/api/tiendas/{nombreTienda}/productos/{id}` | Elimina un producto. | *(Path)* | `200 OK` |
| `GET` | `/api/tiendas/{nombreTienda}/productos/buscar` | Busca productos por nombre. | `q` (Query Requerido) | Lista de `ProductosResponse` |
| `GET` | `/api/tiendas/{nombreTienda}/productos/categoria/{categoriaId}` | Obtiene productos por categoría. | *(Path)* | Lista de `ProductosResponse` |

### Categorías

| Método | Path | Descripción | Response Body |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/tiendas/{nombreTienda}/categorias` | Obtiene todas las categorías de una tienda. | Lista de `CategoriasResponse` |
| `POST` | `/api/tiendas/{nombreTienda}/categorias` | Crea una nueva categoría. | `CategoriasResponse` |
| `GET` | `/api/tiendas/{nombreTienda}/categorias/{id}` | Obtiene una categoría por ID. | `CategoriasResponse` |
| `PATCH` | `/api/tiendas/{nombreTienda}/categorias/{id}` | Actualiza una categoría. | `CategoriasResponse` |
| `DELETE` | `/api/tiendas/{nombreTienda}/categorias/{id}` | Elimina una categoría. | `200 OK` |

### Pedidos

| Método | Path | Descripción | Response Body |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/tiendas/{nombreTienda}/pedidos` | Obtiene todos los pedidos de una tienda. | Lista de `PedidosResponse` |
| `POST` | `/api/tiendas/{nombreTienda}/pedidos` | Crea un nuevo pedido. | `PedidosResponse` |
| `GET` | `/api/tiendas/{nombreTienda}/pedidos/{id}` | Obtiene un pedido por ID. | `PedidosResponse` |
| `PATCH` | `/api/tiendas/{nombreTienda}/pedidos/{id}` | Actualiza el estado de un pedido. | `PedidosResponse` |
| `DELETE` | `/api/tiendas/{nombreTienda}/pedidos/{id}` | Elimina un pedido y sus ítems. | `200 OK` |
| `GET` | `/api/tiendas/{nombreTienda}/pedidos/usuario/{dni}` | Obtiene el historial de pedidos de un usuario. | Lista de `PedidosResponse` |

### Carrito

| Método | Path | Descripción | Response Body |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/tiendas/{nombreTienda}/carrito/agregar` | Agrega un producto al carrito del usuario. | `CarritoResponse` |
| `GET` | `/api/tiendas/{nombreTienda}/carrito/{usuarioDni}` | Muestra el contenido del carrito del usuario. | Lista de `CarritoResponse` |
| `DELETE` | `/api/tiendas/{nombreTienda}/carrito/item/{idItem}` | Elimina un ítem específico del carrito. | `200 OK` |
| `DELETE` | `/api/tiendas/{nombreTienda}/carrito/vaciar/{usuarioDni}` | Vacía completamente el carrito del usuario. | `200 OK` |

### Favoritos

| Método | Path | Descripción | Response Body |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/tiendas/{nombreTienda}/favoritos/toggle` | Agrega o elimina un producto de favoritos. | JSON Object (Mensaje) |
| `GET` | `/api/tiendas/{nombreTienda}/favoritos/{usuarioDni}` | Lista los productos favoritos de un usuario. | Lista de `FavoritoResponse` |

### Almacenamiento y Pagos

| Método | Path | Descripción | Request Body | Response Body |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/storage/upload` | Sube una imagen directamente (Multipart File). | `file` (Binary) | JSON Object (URL de la imagen) |
| `POST` | `/api/pagos/crear/{pedidoId}` | Genera el link de pago de Mercado Pago para un pedido. | `pedidoId` (Path) | JSON Object (Link de pago) |
| `POST` | `/api/pagos/webhook` | Endpoint para recibir notificaciones de Mercado Pago. | `topic`, `id` (Query) | `200 OK` |

---

## Esquemas de Datos (Schemas)

#### **`ProductosResponse`**
Representación completa de un producto.

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `id` | `integer` | ID único del producto. |
| `categoriaId` | `integer` | ID de la categoría a la que pertenece. |
| `nombre` | `string` | Nombre del producto. |
| `descripcion` | `string` | Descripción detallada. |
| `precio` | `double` | Precio del producto. |
| `stock` | `integer` | Cantidad disponible en stock. |
| `categoriaNombre` | `string` | Nombre de la categoría. |
| `imagen` | `string` | URL de la imagen del producto. |

#### **`ProductosRequest`**
Datos necesarios para crear o actualizar un producto.

| Campo | Tipo | Requerido | Restricciones |
| :--- | :--- | :--- | :--- |
| `categoriaId` | `integer` | **Sí** | |
| `nombre` | `string` | **Sí** | Mín. 3, Máx. 100 caracteres |
| `descripcion` | `string` | No | Máx. 500 caracteres |
| `precio` | `double` | **Sí** | |
| `stock` | `integer` | **Sí** | Mín. 0 |

#### **`PedidosResponse`**
Representación de un pedido.

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `id` | `integer` | ID único del pedido. |
| `estado` | `string` | Estado actual del pedido. |
| `total` | `double` | Monto total del pedido. |
| `fechaPedido` | `date-time` | Fecha y hora de creación. |
| `items` | `array` | Lista de ítems del pedido. |
| `usuarioDni` | `integer` | DNI del usuario que realizó el pedido. |

#### **`PedidosRequest`**
Datos para crear o actualizar un pedido.

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `usuarioDni` | `integer` | DNI del usuario que realiza el pedido. |
| `items` | `array` de `ItemsPedidosResponse` | Lista de productos y cantidades a incluir. Puede ser vacío (`[]`). |
| `estado` | `string` | Para actualizar el estado (ej. "CANCELADO"). |
| `metodoEnvio` | `string` | Método de envío seleccionado. |
| `direccionEnvio` | `string` | Dirección de envío completa. |
| `costoEnvio` | `double` | Costo asociado al envío. |

#### **`CategoriasResponse`**
Representación de una categoría.

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `id` | `integer` | ID único de la categoría. |
| `nombre` | `string` | Nombre de la categoría. |

#### **`TiendaResponse`**
Representación de una tienda.

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `id` | `integer` | ID único de la tienda. |
| `nombreUrl` | `string` | Slug o identificador de URL. |
| `nombreFantasia` | `string` | Nombre comercial de la tienda. |
| `logo` | `string` | URL del logo de la tienda. |
| `descripcion` | `string` | Descripción de la tienda. |
| `vendedorDni` | `integer` | DNI del vendedor/administrador. |
| `vendedorNombre` | `string` | Nombre del vendedor. |

#### **`DireccionResponse`**
Representación de una dirección de usuario.

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `id` | `integer` | ID único de la dirección. |
| `calle` | `string` | Nombre de la calle. |
| `numero` | `string` | Número de la calle. |
| `piso` | `string` | Número de piso (opcional). |
| `departamento` | `string` | Número de departamento (opcional). |
| `localidad` | `string` | Localidad. |
| `provincia` | `string` | Provincia. |
| `codigoPostal` | `string` | Código postal. |
| `usuarioDni` | `integer` | DNI del usuario al que pertenece la dirección. |

#### **`CarritoResponse`**
Representación de un ítem dentro del carrito.

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `idItem` | `integer` | ID del ítem en el carrito. |
| `productoId` | `integer` | ID del producto. |
| `nombreProducto` | `string` | Nombre del producto. |
| `imagenProducto` | `string` | URL de la imagen del producto. |
| `precioUnitario` | `double` | Precio del producto. |
| `cantidad` | `integer` | Cantidad de unidades del producto. |
| `subtotal` | `double` | Subtotal calculado. |
| `tiendaId` | `integer` | ID de la tienda a la que pertenece el producto. |
| `nombreTienda` | `string` | Nombre de la tienda (slug). |

#### **`UsuariosResponse`**
Representación de un usuario.

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `dni` | `integer` | Documento Nacional de Identidad. |
| `email` | `string` | Correo electrónico del usuario. |
| `nombre` | `string` | Nombre de pila. |
| `apellido` | `string` | Apellido. |

#### **`UsuariosRequest`**
Datos para la creación/actualización de un usuario.

| Campo | Tipo | Requerido | Restricciones |
| :--- | :--- | :--- | :--- |
| `dni` | `integer` | **Sí** | |
| `email` | `string` | **Sí** | |
| `password` | `string` | **Sí** | Mín. 6, Máx. 2147483647 caracteres |
| `nombre` | `string` | **Sí** | |
| `apellido` | `string` | **Sí** | |

#### **`AuthResponse`**
Respuesta de autenticación.

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `token` | `string` | JSON Web Token (JWT) para usar en peticiones seguras. |

#### **`AuthRequest`**
Petición de inicio de sesión.

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `email` | `string` | Correo electrónico. |
| `password` | `string` | Contraseña. |

#### **`RegisterRequest`**
Petición de registro de usuario.

| Campo | Tipo | Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `dni` | `integer` | **Sí** | Documento Nacional de Identidad. |
| `nombre` | `string` | **Sí** | Nombre de pila. |
| `apellido` | `string` | **Sí** | Apellido. |
| `email` | `string` | **Sí** | Correo electrónico. |
| `password` | `string` | **Sí** | Contraseña. |

---

## Relaciones de la Base de Datos (DB)

A continuación, se describen las relaciones clave (Foreign Keys - FK) entre las entidades principales del sistema, que definen la estructura de la información.

| Entidad Principal | Entidad Relacionada | Tipo de Relación | Campo Clave (FK) | Descripción |
| :--- | :--- | :--- | :--- | :--- |
| **Tiendas** | Usuarios | One-to-One | `vendedorDni` | Cada tienda es administrada por un único usuario (vendedor). |
| **Productos** | Tiendas | Many-to-One | (Implícito) | Cada producto pertenece a una única tienda. |
| **Productos** | Categorías | Many-to-One | `categoriaId` | Cada producto está clasificado bajo una categoría. |
| **Categorías** | Tiendas | Many-to-One | (Implícito) | Las categorías están vinculadas a una tienda específica. |
| **Pedidos** | Usuarios | Many-to-One | `usuarioDni` | Cada pedido es realizado por un único usuario. |
| **Pedidos** | Ítems Pedidos | One-to-Many | (Implícito) | Un pedido contiene una lista de ítems detallados. |
| **Carrito** | Usuarios | Many-to-One | `usuarioDni` | El carrito de compra pertenece a un único usuario. |
| **Direcciones** | Usuarios | Many-to-One | `usuarioDni` | Un usuario puede registrar múltiples direcciones de envío. |
| **Favoritos** | Usuarios | Many-to-One | `usuarioDni` | Un favorito está asociado a un usuario. |
| **Favoritos** | Productos | Many-to-One | `productoId` | Un favorito se refiere a un producto específico. |
