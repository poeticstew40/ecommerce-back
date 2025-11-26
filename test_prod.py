import requests
import json
import os
import time
import sys
import base64

# ================= CONFIGURACIÓN =================
# A) PARA RENDER (Producción)
BASE_URL = "https://ecommerce-back-2uxy.onrender.com/api"

# B) PARA LOCAL (Tu PC)
# BASE_URL = "http://localhost:8080/api"

# DATOS DE PRUEBA
EMAIL = "nicokenrou@gmail.com"
PASSWORD = "password123"
DNI = 22334455 
NOMBRE_TIENDA = "tienda-full-test" 

# COLORES
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

def create_dummy_image(name="test_image.png"):
    # PNG válido de 1x1 pixel
    valid_png_b64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
    with open(name, "wb") as f:
        f.write(base64.b64decode(valid_png_b64))
    return name

def check_response(res, step_name, success_codes=[200, 201]):
    if res.status_code in success_codes:
        return True
    else:
        log(step_name, f"Status: {res.status_code} | Body: {res.text}", "FAIL")
        return False

def run_full_test():
    global token, global_category_id, global_product_id, global_order_id

    print(f"\n{YELLOW}=== INICIANDO TEST FINAL (CON EDICIÓN DE TIENDA) ==={RESET}")
    print(f"Target: {BASE_URL}")

    # ---------------------------------------------------------
    # 1. AUTENTICACIÓN
    # ---------------------------------------------------------
    log("Auth", "Verificando usuario...")
    payload_register = {
        "dni": DNI, "nombre": "Tester", "apellido": "Final", "email": EMAIL, "password": PASSWORD
    }
    try:
        res = session.post(f"{BASE_URL}/auth/register", json=payload_register)
        if res.status_code == 200:
            log("Auth", "Usuario registrado. VERIFICA TU EMAIL y presiona Enter.", "PASS")
            input()
        elif res.status_code == 400:
            log("Auth", "Usuario ya existe, logueando...", "WARN")
    except Exception as e:
        print(f"Error conexión: {e}")
        sys.exit()

    res = session.post(f"{BASE_URL}/auth/login", json={"email": EMAIL, "password": PASSWORD})
    if res.status_code == 200:
        token = res.json().get("token")
        session.headers.update({"Authorization": f"Bearer {token}"})
        log("Auth", "Login exitoso.", "PASS")
    else:
        sys.exit()

    # ---------------------------------------------------------
    # 2. DIRECCIONES
    # ---------------------------------------------------------
    res_list = session.get(f"{BASE_URL}/usuarios/direcciones/{DNI}")
    if res_list.status_code == 200 and len(res_list.json()) == 0:
        dir_payload = { "usuarioDni": DNI, "calle": "Av Test", "numero": "123", "localidad": "CABA", "provincia": "BA", "codigoPostal": "1000" }
        session.post(f"{BASE_URL}/usuarios/direcciones", json=dir_payload)
        log("Direcciones", "Dirección creada.", "PASS")
    else:
        log("Direcciones", "Dirección ya existe.", "PASS")

    # ---------------------------------------------------------
    # 3. CREAR TIENDA (AHORA CON MULTIPART)
    # ---------------------------------------------------------
    log("Tienda", f"Intentando crear tienda '{NOMBRE_TIENDA}'...")
    img_logo = create_dummy_image("logo.png")
    
    tienda_data = {
        "nombreUrl": NOMBRE_TIENDA,
        "nombreFantasia": "Tienda Original",
        "descripcion": "Descripcion inicial",
        "vendedorDni": DNI
    }
    
    # Preparamos el multipart igual que en productos
    multipart_tienda = {
        'tienda': (None, json.dumps(tienda_data), 'application/json'),
        'file': (img_logo, open(img_logo, 'rb'), 'image/png')
    }
    
    res = session.post(f"{BASE_URL}/tiendas", files=multipart_tienda)
    
    if res.status_code in [200, 201]:
        log("Tienda", "Tienda creada exitosamente (Rol actualizado a VENDEDOR).", "PASS")
        logo_url = res.json().get("logo")
        log("Cloudinary", f"Logo inicial: {logo_url}", "PASS")
    elif res.status_code == 400 and "ya está en uso" in res.text:
        log("Tienda", "La tienda ya existía.", "WARN")
    else:
        log("Tienda", f"Error: {res.text}", "FAIL")

    # ---------------------------------------------------------
    # 3.1 EDITAR TIENDA (NUEVO TEST PATCH)
    # ---------------------------------------------------------
    log("Tienda Update", "Probando edición de tienda (Nuevo nombre y logo)...")
    img_new_logo = create_dummy_image("logo_v2.png")
    
    update_data = {
        "nombreUrl": NOMBRE_TIENDA, # Obligatorio para el DTO aunque no cambie
        "nombreFantasia": "Tienda RENOVADA V2",
        "descripcion": "Descripción actualizada por PATCH",
        "vendedorDni": DNI
    }
    
    multipart_update = {
        'tienda': (None, json.dumps(update_data), 'application/json'),
        'file': (img_new_logo, open(img_new_logo, 'rb'), 'image/png')
    }
    
    # Usamos PATCH
    res = session.patch(f"{BASE_URL}/tiendas/{NOMBRE_TIENDA}", files=multipart_update)
    
    if check_response(res, "Editar Tienda"):
        new_name = res.json().get("nombreFantasia")
        new_logo = res.json().get("logo")
        if "RENOVADA" in new_name:
            log("Tienda Update", f"Nombre cambiado a: {new_name}", "PASS")
            log("Tienda Update", f"Logo actualizado: {new_logo}", "PASS")
        else:
            log("Tienda Update", "No se actualizaron los datos.", "FAIL")

    # ---------------------------------------------------------
    # 4. CATEGORÍAS
    # ---------------------------------------------------------
    cat_name = f"Cat-{int(time.time())}"
    res = session.post(f"{BASE_URL}/tiendas/{NOMBRE_TIENDA}/categorias", json={"nombre": cat_name})
    if check_response(res, "Crear Categoría"):
        global_category_id = res.json().get("id")

    # ---------------------------------------------------------
    # 5. PRODUCTOS
    # ---------------------------------------------------------
    log("Productos", "Subiendo producto...")
    img_prod = create_dummy_image("prod.png")
    prod_data = {
        "categoriaId": global_category_id,
        "nombre": f"Prod-{int(time.time())}",
        "descripcion": "Test",
        "precio": 1000.0,
        "stock": 50
    }
    multipart_prod = {
        'producto': (None, json.dumps(prod_data), 'application/json'),
        'file': (img_prod, open(img_prod, 'rb'), 'image/png')
    }
    res = session.post(f"{BASE_URL}/tiendas/{NOMBRE_TIENDA}/productos", files=multipart_prod)
    if check_response(res, "Crear Producto"):
        global_product_id = res.json().get("id")

    # ---------------------------------------------------------
    # 6. PEDIDO
    # ---------------------------------------------------------
    log("Checkout", "Creando pedido...")
    cart_payload = { "usuarioDni": DNI, "productoId": global_product_id, "cantidad": 1 }
    session.post(f"{BASE_URL}/tiendas/{NOMBRE_TIENDA}/carrito/agregar", json=cart_payload)

    pedido_payload = { "usuarioDni": DNI, "metodoEnvio": "Retiro", "costoEnvio": 0.0, "items": [] }
    res = session.post(f"{BASE_URL}/tiendas/{NOMBRE_TIENDA}/pedidos", json=pedido_payload)
    
    if check_response(res, "Crear Pedido"):
        global_order_id = res.json().get("id")
        log("Checkout", f"Pedido #{global_order_id} creado.", "PASS")
        
        print(f"\n{GREEN}=== ✅ TEST FINALIZADO ==={RESET}")
        print(f"Prueba MercadoPago manual con ID: {global_order_id}")

if __name__ == "__main__":
    run_full_test()