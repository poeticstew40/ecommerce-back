import requests
import json
import os
import time

# --- CONFIGURACIÓN ---
BASE_URL = "http://localhost:8080/api"

# DATOS VENDEDOR (Para probar Tienda + Alertas de Seguridad)
EMAIL_VENDEDOR = "nicokenrou@gmail.com"
PASS_VENDEDOR = "Password123!" 
DNI_VENDEDOR = 33445566

# DATOS COMPRADOR (Para probar Compra + Link de Pago)
EMAIL_COMPRADOR = "nicolas_gigena@hotmail.es"
PASS_COMPRADOR = "123456"
DNI_COMPRADOR_REAL = 12345678 

# ESTADO GLOBAL
token_vendedor = None
token_comprador = None
id_tienda = None
id_producto = None
id_pedido = None
nombre_tienda = "tienda-qa-final"

# COLORES
VERDE = "\033[92m"
ROJO = "\033[91m"
AZUL = "\033[94m"
AMARILLO = "\033[93m"
RESET = "\033[0m"

def log_seccion(msg):
    print(f"\n{AZUL}{'='*60}\n{msg}\n{'='*60}{RESET}")

def log_exito(msg):
    print(f"{VERDE}✅ {msg}{RESET}")

def log_error(msg):
    print(f"{ROJO}❌ {msg}{RESET}")

def log_info(msg):
    print(f"{AMARILLO}ℹ️  {msg}{RESET}")

def crear_imagen_dummy():
    try:
        if not os.path.exists("test_image.jpg"):
            log_info("Descargando imagen de prueba...")
            res = requests.get("https://dummyimage.com/400x400/000/fff.jpg")
            if res.status_code == 200:
                with open("test_image.jpg", "wb") as f:
                    f.write(res.content)
    except:
        with open("test_image.jpg", "wb") as f:
            f.write(b'\x00')

# --- PRUEBAS ---

def prueba_1_auth_vendedor():
    global token_vendedor
    log_seccion("PASO 1: AUTENTICACIÓN VENDEDOR & EMAIL BIENVENIDA")
    
    # 1. Registro
    payload = {
        "dni": DNI_VENDEDOR, "nombre": "Nico", "apellido": "QA",
        "email": EMAIL_VENDEDOR, "password": PASS_VENDEDOR
    }
    res = requests.post(f"{BASE_URL}/auth/register", json=payload)
    
    if res.status_code == 200:
        log_exito("Usuario registrado exitosamente.")
        print(f"   📩 {AMARILLO}REVISA {EMAIL_VENDEDOR}: Debe haber llegado el HTML de 'Bienvenido/Verificar'.{RESET}")
        input(f"   {AMARILLO}Haz clic en el link del correo y presiona ENTER aquí...{RESET}")
    elif res.status_code == 400:
        log_info("El usuario ya existía (Asumimos que ya está verificado).")
    
    # 2. Login
    res_login = requests.post(f"{BASE_URL}/auth/login", json={"email": EMAIL_VENDEDOR, "password": PASS_VENDEDOR})
    if res_login.status_code == 200:
        token_vendedor = res_login.json()['token']
        log_exito("Login Vendedor OK")
    else:
        log_error(f"Fallo Login: {res_login.text}")
        exit()

def prueba_2_gestion_tienda():
    global id_tienda, id_producto
    log_seccion("PASO 2: CREACIÓN DE TIENDA & EMAIL 'TIENDA LISTA'")
    headers = {"Authorization": f"Bearer {token_vendedor}"}
    
    # 1. Crear Tienda
    tienda_data = {
        "nombreUrl": nombre_tienda, "nombreFantasia": "Tienda QA Profesional",
        "descripcion": "Probando correos HTML", "vendedorDni": DNI_VENDEDOR
    }
    
    files = {
        'file': ('test_image.jpg', open('test_image.jpg', 'rb'), 'image/jpeg'),
        'tienda': (None, json.dumps(tienda_data), 'application/json')
    }
    
    res = requests.post(f"{BASE_URL}/tiendas", headers=headers, files=files)
    
    if res.status_code in [200, 201]:
        id_tienda = res.json()['id']
        log_exito("Tienda Creada.")
        print(f"   📩 {AMARILLO}REVISA {EMAIL_VENDEDOR}: Debe haber llegado el HTML de '¡Tu Tienda está Lista!' con botón.{RESET}")
    elif "uso" in res.text:
        log_info("La tienda ya existía. Usamos la existente.")
        # Buscamos ID
        res_get = requests.get(f"{BASE_URL}/tiendas/{nombre_tienda}")
        id_tienda = res_get.json()['id']
    else:
        log_error(f"Fallo creando tienda: {res.text}")
        return

    # 2. Crear Producto
    cat_payload = {"nombre": "General"}
    res_cat = requests.post(f"{BASE_URL}/tiendas/{nombre_tienda}/categorias", headers=headers, json=cat_payload)
    cat_id = res_cat.json().get('id', 1)

    prod_payload = {
        "nombre": "Producto QA", "descripcion": "Test",
        "precio": 1500.0, "stock": 100, "categoriaId": cat_id
    }
    files_prod = {
        'file': ('test_image.jpg', open('test_image.jpg', 'rb'), 'image/jpeg'),
        'producto': (None, json.dumps(prod_payload), 'application/json')
    }
    res_prod = requests.post(f"{BASE_URL}/tiendas/{nombre_tienda}/productos", headers=headers, files=files_prod)
    if res_prod.status_code in [200, 201]:
        id_producto = res_prod.json()['id']
        log_exito("Producto Creado OK")

def prueba_3_alertas_seguridad():
    log_seccion("PASO 3: ALERTAS DE SEGURIDAD (PERFIL)")
    headers = {"Authorization": f"Bearer {token_vendedor}"}
    
    # 1. Cambio de Nombre (Update Profile)
    log_info("Cambiando nombre para forzar alerta...")
    update_payload = {
        "dni": DNI_VENDEDOR, "nombre": "Nico Editado", "apellido": "QA Test",
        "email": EMAIL_VENDEDOR # Mismo email
    }
    res_upd = requests.patch(f"{BASE_URL}/usuarios/{DNI_VENDEDOR}", headers=headers, json=update_payload)
    
    if res_upd.status_code == 200:
        log_exito("Perfil actualizado.")
        print(f"   📩 {AMARILLO}REVISA {EMAIL_VENDEDOR}: Debe haber llegado 'Aviso de Seguridad: Actualización de Perfil'.{RESET}")
    else:
        log_error(f"Fallo update: {res_upd.text}")

    time.sleep(1)

    # 2. Cambio de Contraseña (Endpoint seguro)
    log_info("Cambiando contraseña...")
    pass_payload = {"currentPassword": PASS_VENDEDOR, "newPassword": "NuevaPassword123!"}
    res_pass = requests.post(f"{BASE_URL}/auth/change-password", headers=headers, json=pass_payload)
    
    if res_pass.status_code == 200:
        log_exito("Contraseña cambiada.")
        print(f"   📩 {AMARILLO}REVISA {EMAIL_VENDEDOR}: Debe haber llegado 'Seguridad: Cambio de contraseña'.{RESET}")
        
        # Restaurar contraseña original para no romper futuras pruebas
        requests.post(f"{BASE_URL}/auth/change-password", headers=headers, json={"currentPassword": "NuevaPassword123!", "newPassword": PASS_VENDEDOR})
        log_exito("Contraseña restaurada a la original.")
    else:
        log_error(f"Fallo cambio pass: {res_pass.text}")

def prueba_4_flujo_compra():
    global token_comprador, id_pedido
    log_seccion("PASO 4: COMPRA Y GENERACIÓN DE LINK MP")
    
    # 1. Login Comprador
    res = requests.post(f"{BASE_URL}/auth/login", json={"email": EMAIL_COMPRADOR, "password": PASS_COMPRADOR})
    if res.status_code == 200:
        token_comprador = res.json()['token']
        log_exito("Comprador Logueado")
    else:
        log_error("Fallo login comprador")
        return

    headers = {"Authorization": f"Bearer {token_comprador}"}

    # 2. Carrito y Pedido
    requests.post(f"{BASE_URL}/tiendas/{nombre_tienda}/carrito/agregar", headers=headers, json={"usuarioDni": DNI_COMPRADOR_REAL, "productoId": id_producto, "cantidad": 1})
    
    order_payload = {
        "usuarioDni": DNI_COMPRADOR_REAL,
        "metodoEnvio": "Retiro",
        "direccionEnvio": "Local",
        "costoEnvio": 0.0,
        "items": []
    }
    res_order = requests.post(f"{BASE_URL}/tiendas/{nombre_tienda}/pedidos", headers=headers, json=order_payload)
    
    if res_order.status_code == 201:
        id_pedido = res_order.json()['id']
        log_exito(f"Pedido #{id_pedido} creado.")
        
        # 3. Generar Link MP (Prueba de Fuego del MercadoPagoService modificado)
        log_info("Generando preferencia de pago...")
        res_pay = requests.post(f"{BASE_URL}/pagos/crear/{id_pedido}", headers=headers)
        
        if res_pay.status_code == 200:
            link = res_pay.json()['url']
            log_exito("Link de Mercado Pago generado correctamente.")
            print(f"   🔗 URL: {link}")
            print(f"   (Nota: No pagaremos ahora para no esperar el webhook en local)")
        else:
            log_error(f"Fallo al generar link: {res_pay.text}")
            print(f"   Status: {res_pay.status_code}")

    else:
        log_error(f"Fallo al crear pedido: {res_order.text}")

def prueba_5_limpieza():
    log_seccion("PASO 5: LIMPIEZA DE DATOS")
    headers = {"Authorization": f"Bearer {token_vendedor}"}
    res = requests.delete(f"{BASE_URL}/tiendas/{nombre_tienda}", headers=headers)
    if res.status_code == 204:
        log_exito("Tienda de prueba eliminada.")
    else:
        log_error(f"No se pudo borrar la tienda: {res.text}")

# --- EJECUCIÓN ---
if __name__ == "__main__":
    try:
        print(f"{AZUL}INICIANDO TEST DE INTEGRACIÓN COMPLETO (QA){RESET}")
        crear_imagen_dummy()
        
        prueba_1_auth_vendedor()
        prueba_2_gestion_tienda()
        prueba_3_alertas_seguridad()
        prueba_4_flujo_compra()
        prueba_5_limpieza()
        
        print(f"\n{VERDE}✅ PRUEBAS FINALIZADAS.{RESET}")
        print("Recuerda revisar tu bandeja de entrada para confirmar el diseño HTML de los correos.")
        
    except Exception as e:
        log_error(f"Error inesperado en el script: {e}")
    finally:
        if os.path.exists("test_image.jpg"): os.remove("test_image.jpg")