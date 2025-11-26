import requests
import json
import os
import time
import sys
import base64

# ================= CONFIGURACIÓN =================
# A) PARA RENDER (Producción)
BASE_URL = "https://ecommerce-back-2uxy.onrender.com/api"

# B) PARA LOCAL (Tu PC) - Descomentar si usas local
# BASE_URL = "http://localhost:8080/api"

# DATOS DE PRUEBA (LOS TUYOS)
EMAIL = "nicokenrou@gmail.com"
PASSWORD = "password123"
DNI = 22334455  # ✅ DNI RESTAURADO AL QUE YA TIENES VERIFICADO
NOMBRE_TIENDA = "tienda-full-test" 

# COLORES CONSOLA
GREEN = "\033[92m"
RED = "\033[91m"
CYAN = "\033[96m"
YELLOW = "\033[93m"
RESET = "\033[0m"

session = requests.Session()
token = None
global_category_id = None
global_product_id = None
global_order_id = None

def log(step, message, status="INFO"):
    if status == "PASS": 
        print(f"[{GREEN}PASS{RESET}] {step}: {message}")
    elif status == "FAIL": 
        print(f"[{RED}FAIL{RESET}] {step}: {message}")
    elif status == "WARN": 
        print(f"[{YELLOW}WARN{RESET}] {step}: {message}")
    else: 
        print(f"[{CYAN}INFO{RESET}] {step}: {message}")

def create_dummy_image():
    filename = "test_image.png"
    # PNG válido de 1x1 pixel para que Cloudinary no explote
    valid_png_b64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
    
    # Sobrescribimos siempre para asegurar que no sea basura vieja
    with open(filename, "wb") as f:
        f.write(base64.b64decode(valid_png_b64))
    return filename

def check_response(res, step_name, success_codes=[200, 201]):
    if res.status_code in success_codes:
        return True
    else:
        log(step_name, f"Status: {res.status_code} | Body: {res.text}", "FAIL")
        return False

def run_full_test():
    global token, global_category_id, global_product_id, global_order_id

    print(f"\n{YELLOW}=== INICIANDO TEST (CON DNI ORIGINAL: {DNI}) ==={RESET}")
    print(f"Target: {BASE_URL}")

    # ---------------------------------------------------------
    # 1. AUTENTICACIÓN
    # ---------------------------------------------------------
    log("Auth", "Intentando registrar usuario...")
    payload_register = {
        "dni": DNI,
        "nombre": "Tester",
        "apellido": "Automated",
        "email": EMAIL,
        "password": PASSWORD
    }
    
    try:
        res = session.post(f"{BASE_URL}/auth/register", json=payload_register)
        
        # Si da 200 es nuevo -> Pedir verificación
        if res.status_code == 200:
            log("Auth", "Usuario registrado correctamente.", "PASS")
            print(f"\n{YELLOW}>>> 📧 IMPORTANTE: Se envió un correo a {EMAIL}.{RESET}")
            print(f"{YELLOW}>>> Por favor, VERIFICA tu cuenta haciendo clic en el enlace del email.{RESET}")
            input(f"Presiona {GREEN}ENTER{RESET} una vez verificado para continuar... ")
            
        # Si da 400 y dice que ya existe -> Usamos el existente
        elif res.status_code == 400 and ("Ya existe" in res.text or "registrado" in res.text):
            log("Auth", "El usuario ya existe. Saltando al Login...", "WARN")
        else:
            log("Registro", f"Error inesperado: {res.text}", "FAIL")
            sys.exit()

    except Exception as e:
        print(f"Error de conexión: {e}")
        sys.exit()

    log("Auth", "Iniciando sesión...")
    res = session.post(f"{BASE_URL}/auth/login", json={"email": EMAIL, "password": PASSWORD})
    
    if res.status_code == 200:
        token = res.json().get("token")
        session.headers.update({"Authorization": f"Bearer {token}"})
        log("Auth", "Login exitoso. Token configurado.", "PASS")
    else:
        log("Auth", f"No se pudo loguear. Body: {res.text}", "FAIL")
        sys.exit()

    # ---------------------------------------------------------
    # 2. DIRECCIONES
    # ---------------------------------------------------------
    log("Direcciones", "Verificando direcciones...")
    res_list = session.get(f"{BASE_URL}/usuarios/direcciones/{DNI}")
    
    if res_list.status_code == 200 and len(res_list.json()) > 0:
        log("Direcciones", "El usuario ya tiene direcciones.", "PASS")
    else:
        dir_payload = {
            "usuarioDni": DNI,
            "calle": "Av. Test",
            "numero": "123",
            "localidad": "Testing",
            "provincia": "Buenos Aires",
            "codigoPostal": "1111"
        }
        res = session.post(f"{BASE_URL}/usuarios/direcciones", json=dir_payload)
        if check_response(res, "Crear Dirección"):
            log("Direcciones", "Dirección creada exitosamente.", "PASS")

    # ---------------------------------------------------------
    # 3. TIENDA
    # ---------------------------------------------------------
    log("Tienda", f"Verificando/Creando tienda '{NOMBRE_TIENDA}'...")
    tienda_payload = {
        "nombreUrl": NOMBRE_TIENDA,
        "nombreFantasia": "Tienda Full Stack",
        "descripcion": "Test automatizado",
        "vendedorDni": DNI,
        "logo": "https://via.placeholder.com/150"
    }
    res = session.post(f"{BASE_URL}/tiendas", json=tienda_payload)
    
    if res.status_code in [200, 201]:
        log("Tienda", "Tienda creada exitosamente.", "PASS")
    elif res.status_code == 400:
        log("Tienda", "La tienda ya existía (o error 400), continuamos...", "WARN")
    else:
        log("Tienda", f"Error creando tienda: {res.text}", "FAIL")

    # ---------------------------------------------------------
    # 4. CATEGORÍAS
    # ---------------------------------------------------------
    log("Categorías", "Creando categoría nueva...")
    cat_name = f"Cat-{int(time.time())}"
    res = session.post(f"{BASE_URL}/tiendas/{NOMBRE_TIENDA}/categorias", json={"nombre": cat_name})
    
    if check_response(res, "Crear Categoría"):
        global_category_id = res.json().get("id")
        log("Categorías", f"Categoría creada ID: {global_category_id}", "PASS")
    else:
        sys.exit()

    # ---------------------------------------------------------
    # 5. PRODUCTOS (IMAGEN CLOUDINARY)
    # ---------------------------------------------------------
    log("Productos", "Subiendo producto con imagen REAL (fix 500)...")
    img_file = create_dummy_image()
    
    prod_data = {
        "categoriaId": global_category_id,
        "nombre": f"Prod-{int(time.time())}",
        "descripcion": "Producto Final",
        "precio": 1000.0,
        "stock": 50
    }
    
    multipart_data = {
        'producto': (None, json.dumps(prod_data), 'application/json'),
        'file': (img_file, open(img_file, 'rb'), 'image/png')
    }
    
    res = session.post(f"{BASE_URL}/tiendas/{NOMBRE_TIENDA}/productos", files=multipart_data)
    
    if check_response(res, "Crear Producto"):
        global_product_id = res.json().get("id")
        log("Productos", f"Producto creado ID: {global_product_id}", "PASS")
    else:
        sys.exit()

    # ---------------------------------------------------------
    # 6. PEDIDO
    # ---------------------------------------------------------
    log("Carrito", "Agregando al carrito...")
    cart_payload = { "usuarioDni": DNI, "productoId": global_product_id, "cantidad": 1 }
    session.post(f"{BASE_URL}/tiendas/{NOMBRE_TIENDA}/carrito/agregar", json=cart_payload)

    log("Pedido", "Creando pedido...")
    pedido_payload = {
        "usuarioDni": DNI, 
        "metodoEnvio": "Retiro", 
        "costoEnvio": 0.0, 
        "items": []
    }
    res = session.post(f"{BASE_URL}/tiendas/{NOMBRE_TIENDA}/pedidos", json=pedido_payload)
    
    if check_response(res, "Crear Pedido"):
        global_order_id = res.json().get("id")
        total = res.json().get("total")
        log("Pedido", f"Pedido #{global_order_id} CREADO CORRECTAMENTE. Total: ${total}", "PASS")
        
        print(f"\n{GREEN}=== ✅ EXITO HASTA PEDIDO ==={RESET}")
        print(f"\n{YELLOW}--- PASO MANUAL: PROBAR MERCADO PAGO ---{RESET}")
        print(f"1. Copia este ID de Pedido: {CYAN}{global_order_id}{RESET}")
        print(f"2. Copia tu Token JWT (ya estás logueado en Swagger, si no logueate con {EMAIL}).")
        print(f"3. Ve a Swagger -> MercadoPago Controller -> /api/pagos/crear/{{pedidoId}}")
        print(f"4. Pega el ID {global_order_id} y dale Execute.")
        print(f"Si te devuelve el link, ¡ya ganaste!")

    else:
        sys.exit()

if __name__ == "__main__":
    run_full_test()