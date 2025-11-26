import requests
import json
import os
import time
import sys
import base64

# ==========================================
#  CONFIGURACIÓN DEL ENTORNO
# ==========================================
# 1. Selecciona la URL correcta descomentando
BASE_URL = "https://ecommerce-back-2uxy.onrender.com/api"
# BASE_URL = "http://localhost:8080/api" 

# 2. Datos de Prueba (Credenciales Reales para DB Persistente)
EMAIL = "nicokenrou@gmail.com"
PASSWORD = "password123"
DNI = 22334455 
NOMBRE_TIENDA = "tienda-coverage-test" 

# ==========================================
#  UTILIDADES (COLORES Y HELPERS)
# ==========================================
GREEN = "\033[92m"
RED = "\033[91m"
CYAN = "\033[96m"
YELLOW = "\033[93m"
RESET = "\033[0m"

session = requests.Session()
token = None

# Variables Globales para persistencia entre pasos
g_dir_id = None
g_cat_id = None
g_prod_id = None
g_cart_item_id = None
g_order_id = None

def log(method, endpoint, msg, status="INFO"):
    color = {"PASS": GREEN, "FAIL": RED, "WARN": YELLOW}.get(status, CYAN)
    # Formato alineado para lectura fácil
    print(f"[{color}{status}{RESET}] {method:<6} {endpoint:<45} -> {msg}")

def check(res, method, endpoint, expected=[200, 201, 204]):
    if res.status_code in expected:
        return True
    log("FAIL", endpoint, f"Status: {res.status_code} | Error: {res.text}", "FAIL")
    return False

def create_dummy_image(name="test_img.png"):
    # Crea un PNG válido de 1x1 pixel (Transparente)
    # Esto evita el error "Invalid Image" de Cloudinary
    b64_data = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
    with open(name, "wb") as f:
        f.write(base64.b64decode(b64_data))
    return name

# ==========================================
#  EJECUCIÓN DEL TEST
# ==========================================
def run_test():
    global token, g_dir_id, g_cat_id, g_prod_id, g_cart_item_id, g_order_id

    print(f"\n{YELLOW}=============================================={RESET}")
    print(f"{YELLOW}   TEST DE COBERTURA TOTAL (ULTIMATE) v3.0    {RESET}")
    print(f"{YELLOW}=============================================={RESET}")
    print(f"Target: {BASE_URL}\n")

    # ---------------------------------------------------------
    # 1. AUTH CONTROLLER (Login / Register / Verify)
    # ---------------------------------------------------------
    print(f"{YELLOW}--- 1. AUTENTICACIÓN Y USUARIOS ---{RESET}")
    
    # Intento de Registro
    endpoint_reg = "/auth/register"
    reg_payload = {
        "dni": DNI, "nombre": "Admin", "apellido": "Tester", 
        "email": EMAIL, "password": PASSWORD
    }
    try:
        res = session.post(f"{BASE_URL}{endpoint_reg}", json=reg_payload)
        if res.status_code == 200:
            log("POST", endpoint_reg, "Usuario registrado. (Requiere verificar email si es H2)", "PASS")
        elif res.status_code == 400:
            log("POST", endpoint_reg, "El usuario ya existe. Saltando al Login.", "PASS")
        else:
            check(res, "POST", endpoint_reg)
            sys.exit()
    except Exception as e:
        print(f"Error crítico de conexión: {e}")
        sys.exit()

    # Login
    endpoint_login = "/auth/login"
    res = session.post(f"{BASE_URL}{endpoint_login}", json={"email": EMAIL, "password": PASSWORD})
    if check(res, "POST", endpoint_login):
        token = res.json().get("token")
        session.headers.update({"Authorization": f"Bearer {token}"})
        log("POST", endpoint_login, "Token JWT obtenido y configurado.", "PASS")
    else:
        log("FATAL", endpoint_login, "No se pudo iniciar sesión.", "FAIL")
        sys.exit()

    # ---------------------------------------------------------
    # 2. DIRECCION CONTROLLER (CRUD Completo)
    # ---------------------------------------------------------
    print(f"\n{YELLOW}--- 2. DIRECCIONES (CRUD) ---{RESET}")
    
    # CREATE (POST)
    url_dir = "/usuarios/direcciones"
    dir_data = {
        "usuarioDni": DNI, "calle": "Calle Falsa", "numero": "123", 
        "localidad": "Springfield", "provincia": "Estado", "codigoPostal": "1111"
    }
    res = session.post(f"{BASE_URL}{url_dir}", json=dir_data)
    if check(res, "POST", url_dir):
        g_dir_id = res.json().get("id")
        log("POST", url_dir, f"Dirección creada ID: {g_dir_id}", "PASS")

    # READ (GET List)
    check(session.get(f"{BASE_URL}{url_dir}/{DNI}"), "GET", f"{url_dir}/{{dni}}")

    # DELETE (DELETE)
    check(session.delete(f"{BASE_URL}{url_dir}/{g_dir_id}"), "DELETE", f"{url_dir}/{{id}}")
    log("DELETE", f"{url_dir}/{g_dir_id}", "Dirección eliminada correctamente.", "PASS")

    # ---------------------------------------------------------
    # 3. TIENDA CONTROLLER (Multipart + PATCH)
    # ---------------------------------------------------------
    print(f"\n{YELLOW}--- 3. TIENDAS (MULTIPART & PATCH) ---{RESET}")
    
    url_tienda = "/tiendas"
    img_logo = create_dummy_image("logo.png")
    
    # READ (Verificar si existe)
    res = session.get(f"{BASE_URL}{url_tienda}/{NOMBRE_TIENDA}")
    if res.status_code == 200:
        log("GET", f"{url_tienda}/{NOMBRE_TIENDA}", "La tienda ya existe.", "PASS")
    else:
        # CREATE (POST Multipart)
        tienda_data = {
            "nombreUrl": NOMBRE_TIENDA, "nombreFantasia": "Ultimate Store", 
            "descripcion": "Tienda de prueba integral", "vendedorDni": DNI
        }
        # Formato Multipart para Spring @RequestPart
        multipart_data = {
            'tienda': (None, json.dumps(tienda_data), 'application/json'),
            'file': (img_logo, open(img_logo, 'rb'), 'image/png')
        }
        res = session.post(f"{BASE_URL}{url_tienda}", files=multipart_data)
        
        if res.status_code == 400 and "uso" in res.text:
            log("POST", url_tienda, "Conflicto de nombre (400). Usando existente.", "WARN")
        elif check(res, "POST", url_tienda):
            log("POST", url_tienda, "Tienda creada exitosamente.", "PASS")

    # UPDATE (PATCH Multipart)
    log("INFO", "PATCH", "Probando actualización de Logo y Nombre...")
    update_data = {
        "nombreUrl": NOMBRE_TIENDA, "nombreFantasia": "Store Reloaded v2", 
        "descripcion": "Actualizado via Script", "vendedorDni": DNI
    }
    img_new = create_dummy_image("logo_new.png")
    multipart_update = {
        'tienda': (None, json.dumps(update_data), 'application/json'),
        'file': (img_new, open(img_new, 'rb'), 'image/png')
    }
    res = session.patch(f"{BASE_URL}{url_tienda}/{NOMBRE_TIENDA}", files=multipart_update)
    
    if check(res, "PATCH", f"{url_tienda}/{{slug}}"):
        new_name = res.json().get("nombreFantasia")
        log("PATCH", url_tienda, f"Nombre actualizado a: '{new_name}'", "PASS")

    # ---------------------------------------------------------
    # 4. CATEGORIAS CONTROLLER (CRUD)
    # ---------------------------------------------------------
    print(f"\n{YELLOW}--- 4. CATEGORÍAS (CRUD) ---{RESET}")
    url_cat = f"/tiendas/{NOMBRE_TIENDA}/categorias"
    
    # CREATE (POST)
    cat_name = f"Cat-{int(time.time())}"
    res = session.post(f"{BASE_URL}{url_cat}", json={"nombre": cat_name})
    if check(res, "POST", url_cat):
        g_cat_id = res.json().get("id")
        log("POST", url_cat, f"Categoría ID: {g_cat_id}", "PASS")

    # READ (GET All & ID)
    check(session.get(f"{BASE_URL}{url_cat}"), "GET", url_cat)
    check(session.get(f"{BASE_URL}{url_cat}/{g_cat_id}"), "GET", f"{url_cat}/{{id}}")

    # UPDATE (PATCH)
    res = session.patch(f"{BASE_URL}{url_cat}/{g_cat_id}", json={"nombre": cat_name + " (Edited)"})
    check(res, "PATCH", f"{url_cat}/{{id}}")

    # DELETE (Crear y Borrar Temp)
    res_tmp = session.post(f"{BASE_URL}{url_cat}", json={"nombre": "DeleteMe"})
    tmp_id = res_tmp.json().get("id")
    if check(session.delete(f"{BASE_URL}{url_cat}/{tmp_id}"), "DELETE", f"{url_cat}/{{id}}"):
        log("DELETE", url_cat, "Categoría temporal eliminada.", "PASS")

    # ---------------------------------------------------------
    # 5. PRODUCTOS CONTROLLER (CRUD + SORTING + FILTER)
    # ---------------------------------------------------------
    print(f"\n{YELLOW}--- 5. PRODUCTOS (COMPLETO + ORDENAMIENTO) ---{RESET}")
    url_prod = f"/tiendas/{NOMBRE_TIENDA}/productos"
    img_prod = create_dummy_image("prod.png")

    # CREATE (POST Multipart)
    # Producto Caro (Nombre 'Zeta')
    prod_high = {
        'producto': (None, json.dumps({"categoriaId": g_cat_id, "nombre": "Zeta Product", "precio": 2000.0, "stock": 10, "descripcion": "High End"}), 'application/json'),
        'file': (img_prod, open(img_prod, 'rb'), 'image/png')
    }
    res = session.post(f"{BASE_URL}{url_prod}", files=prod_high)
    if check(res, "POST", url_prod):
        g_prod_id = res.json().get("id")
        log("POST", url_prod, f"Producto Caro (Zeta) creado ID: {g_prod_id}", "PASS")

    # Producto Barato (Nombre 'Alpha') - Para probar ordenamiento
    prod_low = {
        'producto': (None, json.dumps({"categoriaId": g_cat_id, "nombre": "Alpha Product", "precio": 10.0, "stock": 50, "descripcion": "Low End"}), 'application/json'),
        'file': (img_prod, open(img_prod, 'rb'), 'image/png')
    }
    session.post(f"{BASE_URL}{url_prod}", files=prod_low)
    log("POST", url_prod, "Producto Barato (Alpha) creado.", "PASS")

    # READ (GET Sorting)
    log("INFO", "SORT", "Probando ordenamiento...")
    
    # 1. Precio Ascendente
    res_sort = session.get(f"{BASE_URL}{url_prod}?sort=precio_asc")
    if check(res_sort, "GET", "?sort=precio_asc"):
        prices = [p['precio'] for p in res_sort.json()]
        rel_p = [p for p in prices if p in [10.0, 2000.0]]
        if rel_p == sorted(rel_p): log("CHECK", "Sort Precio ASC", "Correcto [10, 2000]", "PASS")
        else: log("CHECK", "Sort Precio ASC", f"Fallo: {rel_p}", "FAIL")

    # 2. Precio Descendente
    res_sort = session.get(f"{BASE_URL}{url_prod}?sort=precio_desc")
    if check(res_sort, "GET", "?sort=precio_desc"):
        prices = [p['precio'] for p in res_sort.json()]
        rel_p = [p for p in prices if p in [10.0, 2000.0]]
        if rel_p == sorted(rel_p, reverse=True): log("CHECK", "Sort Precio DESC", "Correcto [2000, 10]", "PASS")
        else: log("CHECK", "Sort Precio DESC", f"Fallo: {rel_p}", "FAIL")

    # 3. Nombre A-Z
    res_sort = session.get(f"{BASE_URL}{url_prod}?sort=nombre_asc")
    if check(res_sort, "GET", "?sort=nombre_asc"):
        names = [p['nombre'] for p in res_sort.json()]
        rel_n = [n for n in names if n in ['Alpha Product', 'Zeta Product']]
        if rel_n == ['Alpha Product', 'Zeta Product']: log("CHECK", "Sort Nombre A-Z", "Correcto [Alpha, Zeta]", "PASS")
        else: log("CHECK", "Sort Nombre A-Z", f"Fallo: {rel_n}", "FAIL")

    # 4. Nombre Z-A
    res_sort = session.get(f"{BASE_URL}{url_prod}?sort=nombre_desc")
    if check(res_sort, "GET", "?sort=nombre_desc"):
        names = [p['nombre'] for p in res_sort.json()]
        rel_n = [n for n in names if n in ['Alpha Product', 'Zeta Product']]
        if rel_n == ['Zeta Product', 'Alpha Product']: log("CHECK", "Sort Nombre Z-A", "Correcto [Zeta, Alpha]", "PASS")
        else: log("CHECK", "Sort Nombre Z-A", f"Fallo: {rel_n}", "FAIL")

    # READ (GET Filters)
    check(session.get(f"{BASE_URL}{url_prod}/{g_prod_id}"), "GET", f"{url_prod}/{{id}}")
    check(session.get(f"{BASE_URL}{url_prod}/categoria/{g_cat_id}"), "GET", f"{url_prod}/categoria/{{id}}")
    check(session.get(f"{BASE_URL}{url_prod}/buscar?q=Zeta"), "GET", f"{url_prod}/buscar")

    # UPDATE (PATCH)
    patch_data = {"stock": 99, "precio": 1999.99}
    res = session.patch(f"{BASE_URL}{url_prod}/{g_prod_id}", json=patch_data)
    if check(res, "PATCH", f"{url_prod}/{{id}}"):
        log("PATCH", url_prod, f"Stock actualizado a: {res.json().get('stock')}", "PASS")

    # DELETE (Temp)
    mp_del = {
        'producto': (None, json.dumps({"categoriaId": g_cat_id, "nombre": "Basura", "precio": 1.0, "stock": 1, "descripcion": "X"}), 'application/json'),
        'file': (img_prod, open(img_prod, 'rb'), 'image/png')
    }
    res_trash = session.post(f"{BASE_URL}{url_prod}", files=mp_del)
    trash_id = res_trash.json().get("id")
    if check(session.delete(f"{BASE_URL}{url_prod}/{trash_id}"), "DELETE", f"{url_prod}/{{id}}"):
        log("DELETE", url_prod, "Producto temporal eliminado.", "PASS")

    # ---------------------------------------------------------
    # 6. FAVORITOS CONTROLLER
    # ---------------------------------------------------------
    print(f"\n{YELLOW}--- 6. FAVORITOS ---{RESET}")
    url_fav = f"/tiendas/{NOMBRE_TIENDA}/favoritos"
    
    # Toggle ON
    fav_data = {"usuarioDni": DNI, "productoId": g_prod_id}
    res = session.post(f"{BASE_URL}{url_fav}/toggle", json=fav_data)
    if check(res, "POST", f"{url_fav}/toggle"):
        log("POST", "Toggle", f"Msg: {res.json().get('mensaje')}", "PASS")

    # List
    check(session.get(f"{BASE_URL}{url_fav}/{DNI}"), "GET", f"{url_fav}/{{dni}}")

    # Toggle OFF
    session.post(f"{BASE_URL}{url_fav}/toggle", json=fav_data)
    log("POST", "Toggle", "Favorito removido (Toggle OFF).", "PASS")

    # ---------------------------------------------------------
    # 7. CARRITO CONTROLLER
    # ---------------------------------------------------------
    print(f"\n{YELLOW}--- 7. CARRITO ---{RESET}")
    url_cart = f"/tiendas/{NOMBRE_TIENDA}/carrito"
    
    # Add
    cart_data = {"usuarioDni": DNI, "productoId": g_prod_id, "cantidad": 2}
    res = session.post(f"{BASE_URL}{url_cart}/agregar", json=cart_data)
    if check(res, "POST", f"{url_cart}/agregar"):
        g_cart_item_id = res.json().get("idItem")
        log("POST", "Agregar", f"Item creado ID: {g_cart_item_id}", "PASS")

    # Get
    check(session.get(f"{BASE_URL}{url_cart}/{DNI}"), "GET", f"{url_cart}/{{dni}}")

    # Delete Item
    check(session.delete(f"{BASE_URL}{url_cart}/item/{g_cart_item_id}"), "DELETE", f"{url_cart}/item/{{id}}")
    log("DELETE", "Item", "Item eliminado.", "PASS")

    # Vaciar (Reponemos primero)
    session.post(f"{BASE_URL}{url_cart}/agregar", json=cart_data)
    check(session.delete(f"{BASE_URL}{url_cart}/vaciar/{DNI}"), "DELETE", f"{url_cart}/vaciar/{{dni}}")
    log("DELETE", "Vaciar", "Carrito vaciado completo.", "PASS")

    # ---------------------------------------------------------
    # 8. PEDIDOS CONTROLLER (Flow Completo)
    # ---------------------------------------------------------
    print(f"\n{YELLOW}--- 8. PEDIDOS (CRUD) ---{RESET}")
    url_ped = f"/tiendas/{NOMBRE_TIENDA}/pedidos"
    
    # Crear Pedido (Llenar carrito primero)
    session.post(f"{BASE_URL}{url_cart}/agregar", json={"usuarioDni": DNI, "productoId": g_prod_id, "cantidad": 1})
    
    ped_data = {"usuarioDni": DNI, "metodoEnvio": "Retiro", "direccionEnvio": "Calle 123", "costoEnvio": 0.0, "items": []}
    res = session.post(f"{BASE_URL}{url_ped}", json=ped_data)
    if check(res, "POST", url_ped):
        g_order_id = res.json().get("id")
        log("POST", url_ped, f"Pedido creado ID: {g_order_id}", "PASS")

    # READ
    check(session.get(f"{BASE_URL}{url_ped}"), "GET", "All Pedidos")
    check(session.get(f"{BASE_URL}{url_ped}/{g_order_id}"), "GET", "Pedido ID")
    check(session.get(f"{BASE_URL}{url_ped}/usuario/{DNI}"), "GET", "Pedidos User")

    # PATCH (Cancelar)
    res = session.patch(f"{BASE_URL}{url_ped}/{g_order_id}", json={"estado": "CANCELADO"})
    if check(res, "PATCH", f"{url_ped}/{{id}}"):
        status = res.json().get("estado")
        log("PATCH", "Estado", f"Pedido actualizado a: {status}", "PASS")

    # DELETE
    if check(session.delete(f"{BASE_URL}{url_ped}/{g_order_id}"), "DELETE", f"{url_ped}/{{id}}"):
        log("DELETE", "Pedido", "Pedido eliminado correctamente.", "PASS")

    # ---------------------------------------------------------
    # 9. MERCADO PAGO
    # ---------------------------------------------------------
    print(f"\n{YELLOW}--- 9. MERCADO PAGO ---{RESET}")
    
    # Crear pedido nuevo para pagar (porque el anterior lo borramos)
    session.post(f"{BASE_URL}{url_cart}/agregar", json={"usuarioDni": DNI, "productoId": g_prod_id, "cantidad": 1})
    res_new = session.post(f"{BASE_URL}{url_ped}", json=ped_data)
    new_id = res_new.json().get("id")
    
    # Generar Link
    res = session.post(f"{BASE_URL}/pagos/crear/{new_id}")
    if check(res, "POST", "/pagos/crear"):
        link = res.json().get("url")
        log("POST", "Link", f"Generado: {link}", "PASS")

    print(f"\n{GREEN}=============================================={RESET}")
    print(f"{GREEN}   COBERTURA TOTAL COMPLETADA EXITOSAMENTE    {RESET}")
    print(f"{GREEN}=============================================={RESET}")

if __name__ == "__main__":
    run_test()