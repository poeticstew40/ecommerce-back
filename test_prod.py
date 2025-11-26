import requests
import json
import os
import time
import sys
import base64

# ================= CONFIGURACIÓN =================
# URL BASE (Selecciona la correcta)
BASE_URL = "https://ecommerce-back-2uxy.onrender.com/api"
# BASE_URL = "http://localhost:8080/api" 

# TUS DATOS DE PRUEBA
EMAIL = "nicokenrou@gmail.com"
PASSWORD = "password123"
DNI = 22334455 
NOMBRE_TIENDA = "tienda-coverage-test" 

# COLORES PARA LOGS
GREEN = "\033[92m"
RED = "\033[91m"
CYAN = "\033[96m"
YELLOW = "\033[93m"
RESET = "\033[0m"

session = requests.Session()
token = None

# Variables Globales (para pasar IDs entre pruebas)
g_dir_id = None
g_cat_id = None
g_prod_id = None
g_cart_item_id = None
g_order_id = None

def log(method, endpoint, message, status="INFO"):
    """Formato: [STATUS] METHOD Endpoint -> Mensaje"""
    if status == "PASS": 
        print(f"[{GREEN}PASS{RESET}] {method:<6} {endpoint:<45} -> {message}")
    elif status == "FAIL": 
        print(f"[{RED}FAIL{RESET}] {method:<6} {endpoint:<45} -> {message}")
    elif status == "WARN": 
        print(f"[{YELLOW}WARN{RESET}] {method:<6} {endpoint:<45} -> {message}")
    else: 
        print(f"[{CYAN}INFO{RESET}] {method:<6} {endpoint:<45} -> {message}")

def check(res, method, endpoint, expected=[200, 201, 204]):
    if res.status_code in expected:
        return True
    log(method, endpoint, f"Status: {res.status_code} | Body: {res.text}", "FAIL")
    return False

def create_dummy_image(name="test_image.png"):
    # Genera un PNG válido de 1x1 pixel en memoria para no tener errores de "Invalid Image"
    valid_png_b64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
    with open(name, "wb") as f:
        f.write(base64.b64decode(valid_png_b64))
    return name

def run_test():
    global token, g_dir_id, g_cat_id, g_prod_id, g_cart_item_id, g_order_id

    print(f"\n{YELLOW}=== INICIANDO TEST MAESTRO DE ENDPOINTS ==={RESET}")
    print(f"Target: {BASE_URL}\n")

    # ==========================================
    # 1. AUTH & USUARIOS
    # ==========================================
    print(f"{YELLOW}--- 1. AUTH & USUARIOS ---{RESET}")
    
    # Registro
    reg_url = "/auth/register"
    reg_data = {"dni": DNI, "nombre": "Master", "apellido": "Tester", "email": EMAIL, "password": PASSWORD}
    try:
        res = session.post(f"{BASE_URL}{reg_url}", json=reg_data)
        if res.status_code == 200:
            log("POST", reg_url, "Usuario registrado. (Requiere verificación manual si es H2)", "PASS")
        elif res.status_code == 400:
            log("POST", reg_url, "Usuario ya existe, procediendo.", "WARN")
        else:
            check(res, "POST", reg_url)
    except Exception as e:
        print(f"Error fatal de conexión: {e}")
        sys.exit()

    # Login
    login_url = "/auth/login"
    res = session.post(f"{BASE_URL}{login_url}", json={"email": EMAIL, "password": PASSWORD})
    if check(res, "POST", login_url):
        token = res.json().get("token")
        session.headers.update({"Authorization": f"Bearer {token}"})
        log("POST", login_url, "Token obtenido exitosamente.", "PASS")
    else:
        sys.exit()

    # ==========================================
    # 2. DIRECCIONES
    # ==========================================
    print(f"\n{YELLOW}--- 2. DIRECCIONES (CRUD) ---{RESET}")
    
    # POST
    url_dir = "/usuarios/direcciones"
    dir_data = {"usuarioDni": DNI, "calle": "Av Test", "numero": "123", "localidad": "CABA", "provincia": "BA", "codigoPostal": "1414"}
    res = session.post(f"{BASE_URL}{url_dir}", json=dir_data)
    if check(res, "POST", url_dir):
        g_dir_id = res.json().get("id")
        log("POST", url_dir, f"Creada ID: {g_dir_id}", "PASS")

    # GET List
    res = session.get(f"{BASE_URL}{url_dir}/{DNI}")
    if check(res, "GET", f"{url_dir}/{{dni}}"):
        count = len(res.json())
        log("GET", f"{url_dir}/{DNI}", f"Se encontraron {count} direcciones.", "PASS")

    # DELETE
    res = session.delete(f"{BASE_URL}{url_dir}/{g_dir_id}")
    if check(res, "DELETE", f"{url_dir}/{{id}}"):
        log("DELETE", f"{url_dir}/{g_dir_id}", "Dirección eliminada.", "PASS")

    # ==========================================
    # 3. TIENDA
    # ==========================================
    print(f"\n{YELLOW}--- 3. TIENDA (Multipart) ---{RESET}")
    url_tienda = "/tiendas"
    img_file = create_dummy_image("logo_test.png")
    
    # POST (Crear)
    tienda_json = {"nombreUrl": NOMBRE_TIENDA, "nombreFantasia": "Master Store", "descripcion": "Testing automation", "vendedorDni": DNI}
    multipart_data = {
        'tienda': (None, json.dumps(tienda_json), 'application/json'),
        'file': (img_file, open(img_file, 'rb'), 'image/png')
    }
    res = session.post(f"{BASE_URL}{url_tienda}", files=multipart_data)
    
    if res.status_code == 400 and "uso" in res.text:
        log("POST", url_tienda, "La tienda ya existe, usaremos esa.", "WARN")
    elif check(res, "POST", url_tienda):
        log("POST", url_tienda, "Tienda creada correctamente.", "PASS")

    # GET (Leer)
    res = session.get(f"{BASE_URL}{url_tienda}/{NOMBRE_TIENDA}")
    check(res, "GET", f"{url_tienda}/{{nombreUrl}}")
    if res.status_code == 200:
        log("GET", f"{url_tienda}/{NOMBRE_TIENDA}", f"Info obtenida: {res.json().get('nombreFantasia')}", "PASS")

    # PATCH (Editar) - Nuevo test
    update_json = {"nombreUrl": NOMBRE_TIENDA, "nombreFantasia": "Master Store V2", "descripcion": "Updated via Script", "vendedorDni": DNI}
    multipart_update = {
        'tienda': (None, json.dumps(update_json), 'application/json'),
        'file': (img_file, open(img_file, 'rb'), 'image/png')
    }
    res = session.patch(f"{BASE_URL}{url_tienda}/{NOMBRE_TIENDA}", files=multipart_update)
    if check(res, "PATCH", f"{url_tienda}/{{nombreUrl}}"):
        log("PATCH", f"{url_tienda}/{NOMBRE_TIENDA}", "Tienda actualizada (Nombre/Logo).", "PASS")

    # ==========================================
    # 4. CATEGORÍAS
    # ==========================================
    print(f"\n{YELLOW}--- 4. CATEGORÍAS ---{RESET}")
    base_cat = f"/tiendas/{NOMBRE_TIENDA}/categorias"
    
    # POST
    cat_name = f"Cat-{int(time.time())}"
    res = session.post(f"{BASE_URL}{base_cat}", json={"nombre": cat_name})
    if check(res, "POST", base_cat):
        g_cat_id = res.json().get("id")
        log("POST", base_cat, f"Creada ID: {g_cat_id}", "PASS")

    # GET All
    check(session.get(f"{BASE_URL}{base_cat}"), "GET", base_cat)
    
    # GET ID
    check(session.get(f"{BASE_URL}{base_cat}/{g_cat_id}"), "GET", f"{base_cat}/{{id}}")

    # PATCH
    res = session.patch(f"{BASE_URL}{base_cat}/{g_cat_id}", json={"nombre": cat_name + " Edited"})
    check(res, "PATCH", f"{base_cat}/{{id}}")

    # DELETE (Creamos una temporal para borrar)
    res_temp = session.post(f"{BASE_URL}{base_cat}", json={"nombre": "Temp Delete"})
    temp_id = res_temp.json().get("id")
    res = session.delete(f"{BASE_URL}{base_cat}/{temp_id}")
    if check(res, "DELETE", f"{base_cat}/{{id}}"):
        log("DELETE", f"{base_cat}/{temp_id}", "Categoría temporal eliminada.", "PASS")

    # ==========================================
    # 5. PRODUCTOS
    # ==========================================
    print(f"\n{YELLOW}--- 5. PRODUCTOS ---{RESET}")
    base_prod = f"/tiendas/{NOMBRE_TIENDA}/productos"
    
    # POST (Multipart)
    prod_json = {"categoriaId": g_cat_id, "nombre": "Prod Test", "descripcion": "Desc", "precio": 100.0, "stock": 50}
    mp_prod = {
        'producto': (None, json.dumps(prod_json), 'application/json'),
        'file': (img_file, open(img_file, 'rb'), 'image/png')
    }
    res = session.post(f"{BASE_URL}{base_prod}", files=mp_prod)
    if check(res, "POST", base_prod):
        g_prod_id = res.json().get("id")
        log("POST", base_prod, f"Creado ID: {g_prod_id}", "PASS")

    # GET All
    check(session.get(f"{BASE_URL}{base_prod}"), "GET", base_prod)

    # GET ID
    check(session.get(f"{BASE_URL}{base_prod}/{g_prod_id}"), "GET", f"{base_prod}/{{id}}")

    # GET Filter Category
    check(session.get(f"{BASE_URL}{base_prod}/categoria/{g_cat_id}"), "GET", f"{base_prod}/categoria/{{id}}")

    # GET Search
    check(session.get(f"{BASE_URL}{base_prod}/buscar?q=Test"), "GET", f"{base_prod}/buscar")

    # PATCH
    res = session.patch(f"{BASE_URL}{base_prod}/{g_prod_id}", json={"precio": 150.0})
    check(res, "PATCH", f"{base_prod}/{{id}}")

    # DELETE (Temp)
    mp_temp = {'producto': (None, json.dumps(prod_json), 'application/json'), 'file': (img_file, open(img_file, 'rb'), 'image/png')}
    res_temp = session.post(f"{BASE_URL}{base_prod}", files=mp_temp)
    temp_id = res_temp.json().get("id")
    res = session.delete(f"{BASE_URL}{base_prod}/{temp_id}")
    if check(res, "DELETE", f"{base_prod}/{{id}}"):
        log("DELETE", f"{base_prod}/{temp_id}", "Producto temporal eliminado.", "PASS")

    # ==========================================
    # 6. FAVORITOS
    # ==========================================
    print(f"\n{YELLOW}--- 6. FAVORITOS ---{RESET}")
    base_fav = f"/tiendas/{NOMBRE_TIENDA}/favoritos"
    
    # POST (Toggle ON)
    fav_data = {"usuarioDni": DNI, "productoId": g_prod_id}
    res = session.post(f"{BASE_URL}{base_fav}/toggle", json=fav_data)
    if check(res, "POST", f"{base_fav}/toggle"):
        log("POST", f"{base_fav}/toggle", f"Msg: {res.json().get('mensaje')}", "PASS")

    # GET List
    check(session.get(f"{BASE_URL}{base_fav}/{DNI}"), "GET", f"{base_fav}/{{dni}}")

    # POST (Toggle OFF)
    res = session.post(f"{BASE_URL}{base_fav}/toggle", json=fav_data)
    if check(res, "POST", f"{base_fav}/toggle"):
        log("POST", f"{base_fav}/toggle", f"Msg: {res.json().get('mensaje')}", "PASS")

    # ==========================================
    # 7. CARRITO
    # ==========================================
    print(f"\n{YELLOW}--- 7. CARRITO ---{RESET}")
    base_cart = f"/tiendas/{NOMBRE_TIENDA}/carrito"
    
    # POST Agregar
    cart_data = {"usuarioDni": DNI, "productoId": g_prod_id, "cantidad": 2}
    res = session.post(f"{BASE_URL}{base_cart}/agregar", json=cart_data)
    if check(res, "POST", f"{base_cart}/agregar"):
        g_cart_item_id = res.json().get("idItem")
        log("POST", f"{base_cart}/agregar", f"Item ID: {g_cart_item_id}", "PASS")

    # GET
    check(session.get(f"{BASE_URL}{base_cart}/{DNI}"), "GET", f"{base_cart}/{{dni}}")

    # DELETE Item
    check(session.delete(f"{BASE_URL}{base_cart}/item/{g_cart_item_id}"), "DELETE", f"{base_cart}/item/{{id}}")

    # DELETE Vaciar (Agregamos de nuevo para probar vaciar)
    session.post(f"{BASE_URL}{base_cart}/agregar", json=cart_data)
    res = session.delete(f"{BASE_URL}{base_cart}/vaciar/{DNI}")
    if check(res, "DELETE", f"{base_cart}/vaciar/{{dni}}"):
        log("DELETE", f"{base_cart}/vaciar/{DNI}", "Carrito vaciado.", "PASS")

    # ==========================================
    # 8. PEDIDOS
    # ==========================================
    print(f"\n{YELLOW}--- 8. PEDIDOS ---{RESET}")
    base_ped = f"/tiendas/{NOMBRE_TIENDA}/pedidos"
    
    # Llenar carrito
    session.post(f"{BASE_URL}{base_cart}/agregar", json={"usuarioDni": DNI, "productoId": g_prod_id, "cantidad": 1})

    # POST Crear
    ped_data = {"usuarioDni": DNI, "metodoEnvio": "Test", "direccionEnvio": "Calle Test", "costoEnvio": 500.0, "items": []}
    res = session.post(f"{BASE_URL}{base_ped}", json=ped_data)
    if check(res, "POST", base_ped):
        g_order_id = res.json().get("id")
        log("POST", base_ped, f"Pedido Creado ID: {g_order_id}", "PASS")

    # GET All
    check(session.get(f"{BASE_URL}{base_ped}"), "GET", base_ped)

    # GET ID
    check(session.get(f"{BASE_URL}{base_ped}/{g_order_id}"), "GET", f"{base_ped}/{{id}}")

    # GET User Orders
    check(session.get(f"{BASE_URL}{base_ped}/usuario/{DNI}"), "GET", f"{base_ped}/usuario/{{dni}}")

    # PATCH (Cancelar)
    res = session.patch(f"{BASE_URL}{base_ped}/{g_order_id}", json={"estado": "CANCELADO"})
    check(res, "PATCH", f"{base_ped}/{{id}}")

    # DELETE
    res = session.delete(f"{BASE_URL}{base_ped}/{g_order_id}")
    if check(res, "DELETE", f"{base_ped}/{{id}}"):
        log("DELETE", f"{base_ped}/{g_order_id}", "Pedido eliminado.", "PASS")

    # ==========================================
    # 9. MERCADO PAGO (Final Check)
    # ==========================================
    print(f"\n{YELLOW}--- 9. MERCADO PAGO ---{RESET}")
    
    # Recreamos un pedido nuevo porque borramos el anterior
    session.post(f"{BASE_URL}{base_cart}/agregar", json={"usuarioDni": DNI, "productoId": g_prod_id, "cantidad": 1})
    res = session.post(f"{BASE_URL}{base_ped}", json=ped_data)
    new_order_id = res.json().get("id")
    
    # Crear Link
    res = session.post(f"{BASE_URL}/pagos/crear/{new_order_id}")
    if check(res, "POST", f"/pagos/crear/{{id}}"):
        url = res.json().get("url")
        log("POST", "/pagos/crear", f"Link generado: {url}", "PASS")

    print(f"\n{GREEN}=== TEST MAESTRO FINALIZADO CON ÉXITO ==={RESET}")

if __name__ == "__main__":
    run_test()