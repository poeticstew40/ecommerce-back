import requests
import json
import os
import time

# --- CONFIGURACIÓN DE USUARIOS ---
BASE_URL = "http://localhost:8080/api"

# 1. VENDEDOR
EMAIL_VENDEDOR = "nicokenrou@gmail.com"
PASS_VENDEDOR = "Password123!" 
DNI_VENDEDOR = 33445566

# 2. COMPRADOR
EMAIL_COMPRADOR = "nicolas_gigena@hotmail.es"
PASS_COMPRADOR = "123456"
# IMPORTANTE: Cambia esto por el DNI real de nicolas_gigena si es distinto en tu BD
DNI_COMPRADOR_REAL = 12345678 

# Variables Globales
token_vendedor = None
token_comprador = None
id_tienda = None
id_producto = None
id_pedido = None
nombre_tienda_url = "tienda-test-final-img" # Cambié el nombre por las dudas

def print_step(msg):
    print(f"\n🔵 {msg}")

def print_ok(msg):
    print(f"   ✅ {msg}")

def print_err(msg):
    print(f"   ❌ {msg}")

def crear_imagen_dummy():
    print("   ⬇️  Descargando una imagen real para la prueba...")
    # Usamos una imagen real (JPG) de un servicio de placeholders
    url_imagen = "https://dummyimage.com/400x400/000/fff.jpg"
    
    try:
        res = requests.get(url_imagen)
        if res.status_code == 200:
            with open("test_image.jpg", "wb") as f:
                f.write(res.content)
            print_ok("Imagen descargada correctamente.")
        else:
            print_err("No se pudo descargar imagen. Se usará archivo vacío (puede fallar).")
            with open("test_image.jpg", "wb") as f:
                f.write(b'\x00') 
    except Exception as e:
        print_err(f"Error descargando imagen: {e}")
        with open("test_image.jpg", "wb") as f:
            f.write(b'\x00')

def ejecutar_pruebas():
    global token_vendedor, token_comprador, id_tienda, id_producto, id_pedido

    print_step("PASO 1: PREPARAR VENDEDOR")
    
    # 1. Registro Vendedor
    payload_reg = {
        "dni": DNI_VENDEDOR, "nombre": "Nico", "apellido": "Vendedor",
        "email": EMAIL_VENDEDOR, "password": PASS_VENDEDOR
    }
    res = requests.post(f"{BASE_URL}/auth/register", json=payload_reg)
    
    if res.status_code == 200:
        print("   ⚠️  USUARIO CREADO. VE A TU CORREO AHORA Y VERIFICALO.")
        input("   Presiona ENTER una vez verificado...")
    elif res.status_code == 400:
        print_ok("El vendedor ya existía (asumimos verificado).")
    
    # 2. Login Vendedor
    res_login_v = requests.post(f"{BASE_URL}/auth/login", json={"email": EMAIL_VENDEDOR, "password": PASS_VENDEDOR})
    if res_login_v.status_code == 200:
        token_vendedor = res_login_v.json()['token']
        print_ok("Vendedor Logueado")
    else:
        print_err(f"No se pudo loguear al vendedor: {res_login_v.text}")
        return

    # 3. Crear Tienda
    print_step("PASO 2: CREAR TIENDA (Con imagen real)")
    headers_v = {"Authorization": f"Bearer {token_vendedor}"}
    
    tienda_data = {
        "nombreUrl": nombre_tienda_url,
        "nombreFantasia": "Tienda Test",
        "descripcion": "Tienda de prueba",
        "vendedorDni": DNI_VENDEDOR
    }
    
    # Abrimos la imagen en modo binario
    try:
        files = {
            'file': ('test_image.jpg', open('test_image.jpg', 'rb'), 'image/jpeg'),
            'tienda': (None, json.dumps(tienda_data), 'application/json')
        }

        res_tienda = requests.post(f"{BASE_URL}/tiendas", headers=headers_v, files=files)
        
        if res_tienda.status_code in [200, 201]:
            id_tienda = res_tienda.json()['id']
            print_ok(f"Tienda Creada (ID: {id_tienda})")
        elif res_tienda.status_code == 400 and "uso" in res_tienda.text:
            print_ok("La tienda ya existía, recuperando datos...")
            res_get = requests.get(f"{BASE_URL}/tiendas/{nombre_tienda_url}")
            id_tienda = res_get.json()['id']
        else:
            print_err(f"FALLÓ CREAR TIENDA (Código {res_tienda.status_code})")
            print(f"   Respuesta: {res_tienda.text}")
            return
    except FileNotFoundError:
        print_err("No se encontró el archivo test_image.jpg. Falló la descarga.")
        return

    # 4. Crear Producto
    print_step("PASO 3: CREAR PRODUCTO")
    
    cat_payload = {"nombre": "General"}
    res_cat = requests.post(f"{BASE_URL}/tiendas/{nombre_tienda_url}/categorias", headers=headers_v, json=cat_payload)
    cat_id = res_cat.json().get('id', 1)

    prod_payload = {
        "nombre": "Producto Test", "descripcion": "Un producto genial",
        "precio": 1500.0, "stock": 100, "categoriaId": cat_id
    }
    # Reusamos la misma imagen
    files_prod = {
        'file': ('test_image.jpg', open('test_image.jpg', 'rb'), 'image/jpeg'),
        'producto': (None, json.dumps(prod_payload), 'application/json')
    }
    
    res_prod = requests.post(f"{BASE_URL}/tiendas/{nombre_tienda_url}/productos", headers=headers_v, files=files_prod)
    
    if res_prod.status_code in [200, 201]:
        id_producto = res_prod.json()['id']
        print_ok(f"Producto Creado (ID: {id_producto})")
    else:
        print_err(f"No se pudo crear producto: {res_prod.text}")
        return

    # 5. LOGIN COMPRADOR
    print_step(f"PASO 4: COMPRA CON USUARIO {EMAIL_COMPRADOR}")
    
    res_login_c = requests.post(f"{BASE_URL}/auth/login", json={"email": EMAIL_COMPRADOR, "password": PASS_COMPRADOR})
    
    if res_login_c.status_code == 200:
        token_comprador = res_login_c.json()['token']
        print_ok("Comprador Logueado")
    else:
        print_err(f"No se pudo loguear al comprador: {res_login_c.text}")
        return

    headers_c = {"Authorization": f"Bearer {token_comprador}"}

    # 6. Carrito
    cart_payload = {
        "usuarioDni": DNI_COMPRADOR_REAL, 
        "productoId": id_producto, 
        "cantidad": 1
    }
    res_cart = requests.post(f"{BASE_URL}/tiendas/{nombre_tienda_url}/carrito/agregar", headers=headers_c, json=cart_payload)
    
    if res_cart.status_code == 200:
        print_ok("Producto agregado al carrito")
    else:
        print_err(f"Error agregando al carrito: {res_cart.text}")
        print("   (Verifica el DNI_COMPRADOR_REAL en el script)")
        return

    # 7. Crear Pedido
    order_payload = {
        "usuarioDni": DNI_COMPRADOR_REAL,
        "metodoEnvio": "Envío a Domicilio",
        "direccionEnvio": "Av. Siempreviva 742",
        "costoEnvio": 500.0,
        "items": []
    }
    res_order = requests.post(f"{BASE_URL}/tiendas/{nombre_tienda_url}/pedidos", headers=headers_c, json=order_payload)
    
    if res_order.status_code == 201:
        id_pedido = res_order.json()['id']
        print_ok(f"Pedido Creado (ID: {id_pedido})")
    else:
        print_err(f"Error creando pedido: {res_order.text}")
        return

    # 8. Pagar
    res_pay = requests.post(f"{BASE_URL}/pagos/crear/{id_pedido}", headers=headers_c)
    
    if res_pay.status_code == 200:
        link = res_pay.json()['url']
        print("\n" + "="*60)
        print(f"💰 LINK DE PAGO PARA {EMAIL_COMPRADOR}:")
        print(f"{link}")
        print("="*60)
        print("1. Abre el link.")
        print("2. Paga con credenciales de prueba de MP.")
        print("3. Revisa los correos (Comprador y Vendedor deberían recibir aviso).")
        input("Presiona ENTER para finalizar...")
    else:
        print_err(f"Error generando pago: {res_pay.text}")

    # 9. Limpieza
    print_step("PASO 5: LIMPIEZA (Borrar Tienda)")
    res_del = requests.delete(f"{BASE_URL}/tiendas/{nombre_tienda_url}", headers=headers_v)
    if res_del.status_code == 204:
        print_ok("Tienda eliminada correctamente.")
    else:
        print_err(f"Fallo al eliminar tienda: {res_del.text}")

if __name__ == "__main__":
    crear_imagen_dummy()
    try:
        ejecutar_pruebas()
    except Exception as e:
        print(f"\n❌ ERROR DE EJECUCIÓN: {e}")
    finally:
        if os.path.exists("test_image.jpg"): os.remove("test_image.jpg")