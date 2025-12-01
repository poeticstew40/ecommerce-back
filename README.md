# Documentación del Proyecto - Backend E-commerce Multitienda

Este repositorio contiene el código fuente del backend para la plataforma de E-commerce. A continuación se detallan las instrucciones para la configuración del entorno de desarrollo local y la documentación general de la API.

## Guía de Configuración y Ejecución Local

Para ejecutar este proyecto en un entorno local, es necesario configurar las variables de entorno para garantizar la seguridad de las credenciales y la correcta conexión con los servicios externos.

### Prerrequisitos
* **Java JDK 21**
* **Maven** (Apache Maven 3.8 o superior)

### Instrucciones de Instalación

**1. Clonar el repositorio**
Descargue el código fuente utilizando el siguiente comando en su terminal:

```bash
git clone <URL_DEL_REPOSITORIO>
cd ecommerce
```

**2. Configuración de Variables de Entorno (.env)**
Por motivos de seguridad, las credenciales sensibles no se incluyen en el repositorio. Para ejecutar la aplicación, debe crear un archivo llamado `.env` en la raíz del proyecto (al mismo nivel que el archivo `pom.xml`).

Copie el siguiente contenido en su archivo `.env`. Este ejemplo está preconfigurado para utilizar una base de datos en memoria (H2) para facilitar la corrección sin necesidad de instalar MySQL localmente:

```properties
#------------------ Solicitar credenciales reales ------------------

# Configuración de Base de Datos (Por defecto: H2 en memoria)
JDBC_DATABASE_URL=jdbc:h2:mem:ecommerce_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
JDBC_DATABASE_USERNAME=sa
JDBC_DATABASE_PASSWORD=password

# Integraciones y Servicios Externos
RESEND_API_KEY=re_123456789
MP_ACCESS_TOKEN=TEST-00000000-0000-0000-0000-000000000000
JWT_SECRET_KEY=clave_secreta_para_desarrollo_local

# Configuración de Cloudinary (Gestión de Imágenes)
CLOUDINARY_CLOUD_NAME=nombre_cloud
CLOUDINARY_API_KEY=000000000
CLOUDINARY_API_SECRET=secreto_api

# URLs de la Aplicación
APP_BACKEND_URL=http://localhost:8080
APP_FRONTEND_URL=http://localhost:5173
```

**3. Ejecución de la Aplicación**
Una vez configurado el archivo `.env`, inicie la aplicación utilizando Maven. El sistema detectará automáticamente el perfil de desarrollo.

```bash
mvn spring-boot:run
```

**4. Verificación**
Si la aplicación inició correctamente, podrá acceder a:

* **Documentación Interactiva (Swagger):** http://localhost:8080/swagger-ui/index.html
* **Consola de Base de Datos (H2):** http://localhost:8080/h2

---

## Documentación General de la API

### Información de Entornos

* **URL Base (Google Cloud)(Producción):**`https://ecommerce-back-1018928649112.us-central1.run.app`
* **Documentación Swagger UI(Google Cloud):** `https://ecommerce-back-1018928649112.us-central1.run.app/swagger-ui/index.html`

* **URL Base (Render)(Producción):** `https://ecommerce-back-2uxy.onrender.com`
* **Documentación Swagger UI(Render):** `https://ecommerce-back-2uxy.onrender.com/swagger-ui/index.html`

> **Nota para el equipo de Frontend:** Se recomienda utilizar Swagger UI para probar los endpoints y comprender la estructura de los objetos JSON requeridos antes de la integración.

### 1. Autenticación y Seguridad (JWT)
El sistema implementa seguridad basada en JSON Web Tokens (JWT).

* **Obtención del Token:** Al registrarse (`/api/auth/register`) o iniciar sesión (`/api/auth/login`), la API devolverá un token.
* **Uso del Token:** Para acceder a rutas protegidas, este token debe enviarse en el encabezado (Header) `Authorization` de cada petición HTTP con el prefijo `Bearer`.
* **Formato:** `Authorization: Bearer <token_jwt>`

### 2. Arquitectura Multitienda
La plataforma soporta múltiples tiendas operando simultáneamente. Esto se gestiona mediante un identificador único o "slug" en la URL.

* **Estructura del Endpoint:** `/api/tiendas/{nombreTienda}/...`
* **Importante:** Las operaciones sobre productos, categorías o pedidos deben coincidir con la tienda especificada en la URL. Intentar acceder a recursos de una tienda A usando la URL de la tienda B resultará en un error de seguridad.

### 3. Gestión de Imágenes
Para la carga de imágenes en Productos y Tiendas, la API utiliza `multipart/form-data`.

* El archivo de imagen se debe enviar en el campo clave `file`.
* Los datos del objeto (JSON) se deben enviar en el campo clave `producto` o `tienda` con el `Content-Type` configurado como `application/json`.

### 4. Flujo de Compra
El ciclo de vida de una compra sigue estos pasos estrictos:

1. **Gestión del Carrito:** Los productos se añaden al carrito del usuario mediante una petición POST al endpoint `/api/tiendas/{nombre}/carrito/agregar`. Este carrito persiste en la base de datos.
2. **Generación del Pedido (Checkout):** Para finalizar la compra, se envía una petición POST a `/api/tiendas/{nombre}/pedidos`.
3. **Automatización:** Si el cuerpo de la petición envía una lista de ítems vacía (`[]`), el backend procesará automáticamente todos los productos que el usuario tenga actualmente en su carrito y vaciará el carrito tras crear el pedido.
4. **Procesamiento del Pago:** Con el ID del pedido generado en el paso anterior, se llama a `/api/pagos/crear/{id}`. Esto retornará la URL de la pasarela de Mercado Pago para que el usuario realice el pago efectivo.

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
