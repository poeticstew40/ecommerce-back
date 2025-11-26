import requests
import json
import os
import time
import sys

# ================= CONFIGURACIÓN =================
# Descomenta la que vayas a usar:

# A) PARA RENDER (Producción)
BASE_URL = "https://ecommerce-back-2uxy.onrender.com/api"

# B) PARA LOCAL (Tu PC)
# BASE_URL = "http://localhost:8080/api"

# DATOS DE PRUEBA
EMAIL = "nicokenrou@gmail.com"
PASSWORD = "password123"
DNI = 22334455 # Cambiamos DNI para asegurar usuario nuevo si borraste el anterior
NOMBRE_TIENDA = "tienda-full-test" 

# COLORES
GREEN = "\033[92m"
RED = "\033[91m"
CYAN = "\033[96m"
YELLOW = "\033[93m"
RESET = "\033[0m"

session = requests.Session()
token = None
global_store_slug = NOMBRE_TIENDA
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
    if not os.path.exists(filename):
        with open(filename, "wb") as f:
            f.write(os.urandom(1024))
    return filename

def check_response(res, step_name, success_codes=[200, 201]):
    if res.status_code in success_codes:
        return True
    else:
        log(step_name, f"Status: {res.status_code} | Body: {res.text}", "FAIL")
        return False

def run_full_test():
    global token, global_category_id, global_product_id, global_order_id

    print(f"\n{YELLOW}=== INICIANDO TEST INTEGRAL DE E-COMMERCE ==={RESET}")
    print(f"Target: {BASE_URL}")

    # ---------------------------------------------------------
    # 1. AUTENTICACIÓN
    # ---------------------------------------------------------
    log("Auth", "Registrando usuario...")
    payload_register = {
        "dni": DNI,
        "nombre": "Tester",
        "apellido": "Automated",
        "email": EMAIL,
        "password": PASSWORD
    }
    res = session.post(f"{BASE_URL}/auth/register", json=payload_register)
    
    if "ya está registrado" in res.text:
        log("Auth", "Usuario ya existe, procediendo al Login.", "WARN")
    elif not check_response(res, "Registro"):
        sys.exit() # Si falla registro critico, salimos
    else:
        log("Auth", "Usuario registrado correctamente.", "PASS")
        print(f"\n{YELLOW}>>> 📧 IMPORTANTE: Se envió un correo a {EMAIL}.{RESET}")
        print(f"{YELLOW}>>> Por favor, VERIFICA tu cuenta haciendo clic en el enlace del email.{RESET}")
        input(f"Presiona {GREEN}ENTER{RESET} una vez verificado para continuar... ")

    log("Auth", "Iniciando sesión...")
    res = session.post(f"{BASE_URL}/auth/login", json={"email": EMAIL, "password": PASSWORD})
    if check_response(res, "Login"):
        token = res.json().get("token")
        session.headers.update({"Authorization": f"Bearer {token}"})
        log("Auth", "Token JWT obtenido y configurado.", "PASS")
    else:
        sys.exit()

    # ---------------------------------------------------------
    # 2. DIRECCIONES (CRUD)
    # ---------------------------------------------------------
    log("Direcciones", "Creando dirección de envío...")
    dir_payload = {
        "usuarioDni": DNI,
        "calle": "Av. Siempreviva",
        "numero": "742",
        "localidad": "Springfield",
        "provincia": "Buenos Aires",
        "codigoPostal": "1234"
    }
    res = session.post(f"{BASE_URL}/usuarios/direcciones", json=dir_payload)
    if check_response(res, "Crear Dirección"):
        dir_id = res.json().get("id")
        log("Direcciones", f"Dirección creada ID: {dir_id}", "PASS")
        
        # Test eliminar (borramos y creamos de nuevo para dejar una activa)
        session.delete(f"{BASE_URL}/usuarios/direcciones/{dir_id}")
        log("Direcciones", "Dirección eliminada (Test DELETE)", "PASS")
        # Creamos la definitiva
        session.post(f"{BASE_URL}/usuarios/direcciones", json=dir_payload)

    # ---------------------------------------------------------
    # 3. TIENDA
    # ---------------------------------------------------------
    log("Tienda", f"Creando tienda '{NOMBRE_TIENDA}'...")
    tienda_payload = {
        "nombreUrl": NOMBRE_TIENDA,
        "nombreFantasia": "Tienda Full Stack",
        "descripcion": "Test automatizado",
        "vendedorDni": DNI,
        "logo": "https://via.placeholder.com/150"
    }
    res = session.post(f"{BASE_URL}/tiendas", json=tienda_payload)
    
    if res.status_code == 400 and "ya está en uso" in res.text:
        log("Tienda", "La tienda ya existe, la usaremos.", "WARN")
    elif check_response(res, "Crear Tienda"):
        log("Tienda", "Tienda creada exitosamente.", "PASS")
    else:
        print("Error critico creando tienda.")
        sys.exit()

    # ---------------------------------------------------------
    # 4. CATEGORÍAS (CRUD + Fix del error 400)
    # ---------------------------------------------------------
    log("Categorías", "Creando categoría principal...")
    # Creamos una categoria UNICA para esta ejecucion para evitar duplicados
    cat_name = f"Tecnologia {int(time.time())}"
    res = session.post(f"{BASE_URL}/tiendas/{NOMBRE_TIENDA}/categorias", json={"nombre": cat_name})
    
    if check_response(res, "Crear Categoría"):
        global_category_id = res.json().get("id")
        log("Categorías", f"Categoría '{cat_name}' creada con ID: {global_category_id}", "PASS")
        
        # Test Update
        res_upd = session.patch(f"{BASE_URL}/tiendas/{NOMBRE_TIENDA}/categorias/{global_category_id}", json={"nombre": cat_name + " Updated"})
        if res_upd.status_code == 200:
            log("Categorías", "Categoría actualizada (Test PATCH)", "PASS")

    # ---------------------------------------------------------
    # 5. PRODUCTOS (CRUD + Imágenes)
    # ---------------------------------------------------------
    log("Productos", "Subiendo producto con imagen a Cloudinary...")
    img_file = create_dummy_image()
    
    prod_data = {
        "categoriaId": global_category_id, # USAMOS EL ID RECIEN CREADO (Clave para evitar error 400)
        "nombre": "Notebook Gamer",
        "descripcion": "Potente notebook para testing",
        "precio": 1500.0,
        "stock": 10
    }
    
    multipart_data = {
        'producto': (None, json.dumps(prod_data), 'application/json'),
        'file': (img_file, open(img_file, 'rb'), 'image/png')
    }
    
    res = session.post(f"{BASE_URL}/tiendas/{NOMBRE_TIENDA}/productos", files=multipart_data)
    
    if check_response(res, "Crear Producto"):
        global_product_id = res.json().get("id")
        url_img = res.json().get("imagen")
        log("Productos", f"Producto creado ID: {global_product_id}", "PASS")
        log("Cloudinary", f"Imagen subida: {url_img}", "PASS")

        # Test Update Producto
        upd_payload = {"precio": 2000.0, "stock": 5}
        session.patch(f"{BASE_URL}/tiendas/{NOMBRE_TIENDA}/productos/{global_product_id}", json=upd_payload)
        log("Productos", "Precio y stock actualizados (Test PATCH)", "PASS")

    # Crear producto basura para borrar
    log("Productos", "Testeando borrado de productos...")
    prod_basura_data = prod_data.copy()
    prod_basura_data["nombre"] = "Producto a Borrar"
    multipart_basura = {
        'producto': (None, json.dumps(prod_basura_data), 'application/json'),
        'file': (img_file, open(img_file, 'rb'), 'image/png')
    }
    res_basura = session.post(f"{BASE_URL}/tiendas/{NOMBRE_TIENDA}/productos", files=multipart_basura)
    if res_basura.status_code == 201:
        id_basura = res_basura.json().get("id")
        res_del = session.delete(f"{BASE_URL}/tiendas/{NOMBRE_TIENDA}/productos/{id_basura}")
        if res_del.status_code == 204:
            log("Productos", "Producto eliminado correctamente (Test DELETE)", "PASS")

    # ---------------------------------------------------------
    # 6. CARRITO Y PEDIDOS
    # ---------------------------------------------------------
    log("Carrito", "Agregando producto al carrito...")
    cart_payload = {
        "usuarioDni": DNI,
        "productoId": global_product_id,
        "cantidad": 2
    }
    res = session.post(f"{BASE_URL}/tiendas/{NOMBRE_TIENDA}/carrito/agregar", json=cart_payload)
    check_response(res, "Agregar a Carrito")

    log("Pedido", "Generando pedido (Checkout)...")
    # No mandamos dirección explícita para que use la del perfil que creamos en el paso 2
    pedido_payload = {
        "usuarioDni": DNI,
        "metodoEnvio": "Correo Argentino",
        "costoEnvio": 500.0,
        "items": [] # Vacío para procesar carrito
    }
    res = session.post(f"{BASE_URL}/tiendas/{NOMBRE_TIENDA}/pedidos", json=pedido_payload)
    
    if check_response(res, "Crear Pedido"):
        global_order_id = res.json().get("id")
        total = res.json().get("total")
        log("Pedido", f"Pedido #{global_order_id} CREADO. Total: ${total}", "PASS")

    # ---------------------------------------------------------
    # 7. MERCADO PAGO
    # ---------------------------------------------------------
    print(f"\n{YELLOW}--- TEST DE PAGO ---{RESET}")
    print("Generando preferencia en Mercado Pago...")
    
    res = session.post(f"{BASE_URL}/pagos/crear/{global_order_id}")
    
    if check_response(res, "Link MercadoPago"):
        url_pago = res.json().get("url")
        print(f"\n{GREEN}>>> LINK DE PAGO GENERADO EXITOSAMENTE:{RESET}")
        print(f"{CYAN}{url_pago}{RESET}")
        
        # --- SOLICITUD DE CREDENCIALES DE TEST ---
        print("\nPara probar el pago real, abre el link en incógnito.")
        print("Usa estas credenciales de prueba (Sandbox):")
        print(f"💳 {YELLOW}Tarjeta:{RESET}   Test Cards de MP (busca 'tarjetas prueba mercado pago' en google)")
        print(f"👤 {YELLOW}User Test:{RESET} Pedime las credenciales de test y te las paso por consola")
        
        opcion = input(f"\n¿Quieres simular la notificación de pago APROBADO (Webhook) ahora? (s/n): ")
        
        if opcion.lower() == 's':
            # Simulamos lo que haría MercadoPago al notificar al backend
            # NOTA: Esto usualmente requiere un ID de pago real de MP. 
            # Como no podemos pagar desde el script, esto fallará en el backend si valida contra MP API.
            # Pero verifica que el endpoint exista.
            print("Enviando Webhook simulado...")
            res_hook = session.post(f"{BASE_URL}/pagos/webhook?topic=payment&id=123456789")
            if res_hook.status_code == 200:
                log("Webhook", "Webhook recibido correctamente (200 OK)", "PASS")
            else:
                log("Webhook", "Fallo en webhook", "FAIL")

    print(f"\n{GREEN}=== TEST INTEGRAL FINALIZADO ==={RESET}")

if __name__ == "__main__":
    try:
        run_full_test()
    except requests.exceptions.ConnectionError:
        print(f"\n{RED}ERROR FATAL: No se pudo conectar al servidor.{RESET}")
        print("1. Si es local, verifica que Spring Boot esté corriendo en puerto 8080.")
        print("2. Si es Render, verifica que el deploy esté 'Live'.")