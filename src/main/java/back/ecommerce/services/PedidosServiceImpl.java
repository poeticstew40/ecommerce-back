package back.ecommerce.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import back.ecommerce.dtos.ItemsPedidosRequest;
import back.ecommerce.dtos.ItemsPedidosResponse; // Asegurate de tener este import
import back.ecommerce.dtos.PedidosRequest;
import back.ecommerce.dtos.PedidosResponse;
import back.ecommerce.entities.ItemsPedidosEntity;
import back.ecommerce.entities.PedidosEntity;
import back.ecommerce.repositories.CarritoRepository; // ✅ NUEVO
import back.ecommerce.repositories.PedidosRepository;
import back.ecommerce.repositories.ProductosRepository;
import back.ecommerce.repositories.TiendaRepository;
import back.ecommerce.repositories.UsuariosRepository;
import lombok.AllArgsConstructor;

@Service
@Transactional
@AllArgsConstructor
public class PedidosServiceImpl implements PedidosService {

    private final PedidosRepository pedidosRepository;
    private final UsuariosRepository usuariosRepository;
    private final ProductosRepository productosRepository;
    private final TiendaRepository tiendaRepository;
    private final CarritoRepository carritoRepository; // ✅ INYECTADO

    @Override
    public PedidosResponse create(String nombreTienda, PedidosRequest pedidoRequest) {
        
        // 1. Buscamos la TIENDA
        var tienda = tiendaRepository.findByNombreUrl(nombreTienda)
                .orElseThrow(() -> new IllegalArgumentException("Tienda no encontrada: " + nombreTienda));

        // 2. Buscamos el Usuario
        var usuario = usuariosRepository.findById(pedidoRequest.getUsuarioDni())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con DNI: " + pedidoRequest.getUsuarioDni()));

        // --- LÓGICA DE ORIGEN DE ITEMS (Carrito vs Directo) ---
        List<ItemsPedidosRequest> itemsParaProcesar = new ArrayList<>();
        boolean vieneDelCarrito = false;

        // Si el request NO trae items, asumimos que es una compra desde el CARRITO
        if (pedidoRequest.getItems() == null || pedidoRequest.getItems().isEmpty()) {
            var itemsCarrito = carritoRepository.findByUsuarioDni(usuario.getDni());
            
            if (itemsCarrito.isEmpty()) {
                throw new IllegalArgumentException("El carrito está vacío y no se enviaron items manuales.");
            }

            // Convertimos ItemCarrito -> ItemPedidosRequest (DTO interno para procesar igual)
            itemsParaProcesar = itemsCarrito.stream().map(itemCart -> {
                return ItemsPedidosRequest.builder()
                        .productoId(itemCart.getProducto().getId())
                        .cantidad(itemCart.getCantidad())
                        .build();
            }).collect(Collectors.toList());
            
            vieneDelCarrito = true;
        } else {
            // Si trae items, es una compra directa ("Comprar Ahora")
            // Convertimos la lista que viene del Request al tipo que necesitamos procesar
            itemsParaProcesar = pedidoRequest.getItems().stream().map(item -> {
                return ItemsPedidosRequest.builder()
                        .productoId(item.getIdProducto()) // Ojo: ItemsPedidosResponse usa 'idProducto'
                        .cantidad(item.getCantidad())
                        .build();
            }).collect(Collectors.toList());
        }
        // -------------------------------------------------------

        // 3. Armamos el Pedido base
        var pedidoEntity = new PedidosEntity();
        pedidoEntity.setUsuario(usuario);
        pedidoEntity.setTienda(tienda);
        pedidoEntity.setFechaPedido(LocalDateTime.now());
        pedidoEntity.setEstado("PENDIENTE");
        pedidoEntity.setItemsPedido(new ArrayList<>());
        
        // Seteamos datos de envío
        pedidoEntity.setMetodoEnvio(pedidoRequest.getMetodoEnvio());
        // Lógica inteligente de dirección (si viene vacía, usar la del usuario)
        if (pedidoRequest.getDireccionEnvio() == null || pedidoRequest.getDireccionEnvio().isBlank()) {
             
             // Verificamos si tiene direcciones guardadas en su perfil
             if (usuario.getDirecciones() != null && !usuario.getDirecciones().isEmpty()) {
                 // Agarramos la primera dirección de la lista (o podrías buscar la "principal")
                 var dir = usuario.getDirecciones().get(0); 
                 
                 // La convertimos a String para guardarla en el pedido (Snapshot)
                 String direccionTexto = dir.getCalle() + " " + dir.getNumero() + ", " + 
                                         dir.getLocalidad() + " (" + dir.getProvincia() + ")";
                 
                 pedidoEntity.setDireccionEnvio(direccionTexto);
             } else {
                 // Si no mandó nada y no tiene perfil, explotamos
                 throw new IllegalArgumentException("Debes ingresar una dirección de envío o cargar una en tu perfil.");
             }
        } else {
             // Si mandó una dirección específica en el request, usamos esa
             pedidoEntity.setDireccionEnvio(pedidoRequest.getDireccionEnvio());
        }
        
        pedidoEntity.setCostoEnvio(pedidoRequest.getCostoEnvio() != null ? pedidoRequest.getCostoEnvio() : 0.0);

        BigDecimal totalCalculado = BigDecimal.ZERO;

        // 4. Procesamos Items (Reutilizamos la lógica de validación y stock)
        for (var itemReq : itemsParaProcesar) {
            // Nota: Si usaste "idProducto" en el DTO, cambia getProductoId() por getIdProducto() según corresponda
            var producto = productosRepository.findById(itemReq.getProductoId()) 
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con id: " + itemReq.getProductoId()));

            // ✅ VALIDACIÓN A: Coherencia de Tienda
            if (!producto.getTienda().getId().equals(tienda.getId())) {
                throw new IllegalArgumentException("El producto '" + producto.getNombre() + "' no pertenece a la tienda '" + nombreTienda + "'");
            }

            // ✅ VALIDACIÓN B: Control de Stock
            if (producto.getStock() < itemReq.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente para: " + producto.getNombre() + ". Disponible: " + producto.getStock());
            }

            // ✅ ACCIÓN: Descontar Stock
            producto.setStock(producto.getStock() - itemReq.getCantidad());
            productosRepository.save(producto);

            // Crear el Item del Pedido
            var itemEntity = new ItemsPedidosEntity();
            itemEntity.setCantidad(itemReq.getCantidad());
            itemEntity.setProducto(producto);
            itemEntity.setPrecioUnitario(producto.getPrecio());
            itemEntity.setPedido(pedidoEntity);
            
            pedidoEntity.getItemsPedido().add(itemEntity);

            // Calcular total
            BigDecimal cantidad = new BigDecimal(itemReq.getCantidad());
            BigDecimal subtotal = BigDecimal.valueOf(producto.getPrecio()).multiply(cantidad);
            totalCalculado = totalCalculado.add(subtotal);
        }

        // Sumar envío
        if (pedidoEntity.getCostoEnvio() != null && pedidoEntity.getCostoEnvio() > 0) {
            totalCalculado = totalCalculado.add(BigDecimal.valueOf(pedidoEntity.getCostoEnvio()));
        }

        pedidoEntity.setTotal(totalCalculado.doubleValue());
        var pedidoGuardado = pedidosRepository.save(pedidoEntity);

        // ✅ PASO FINAL: Si vino del carrito, lo vaciamos
        if (vieneDelCarrito) {
            carritoRepository.deleteByUsuarioDni(usuario.getDni());
        }

        return convertirEntidadAResponse(pedidoGuardado);
    }

    // ... (El resto de los métodos readAll, readById, update, delete y el helper siguen IGUAL) ...
    
    @Override
    public List<PedidosResponse> readAllByTienda(String nombreTienda) {
        return pedidosRepository.findByTiendaNombreUrl(nombreTienda).stream()
                .map(this::convertirEntidadAResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PedidosResponse> findByUsuarioDni(String nombreTienda, Long dni) {
        return pedidosRepository.findByTiendaNombreUrlAndUsuarioDni(nombreTienda, dni).stream()
                .map(this::convertirEntidadAResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PedidosResponse readById(Long id) {
        var entity = pedidosRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + id));
        return convertirEntidadAResponse(entity);
    }

    @Override
    public PedidosResponse update(Long id, PedidosRequest pedidoRequest) {
        var entityFromDB = pedidosRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + id));

        if (pedidoRequest.getEstado() != null && !pedidoRequest.getEstado().isBlank()) {
            entityFromDB.setEstado(pedidoRequest.getEstado());
        }

        return convertirEntidadAResponse(pedidosRepository.save(entityFromDB));
    }

    @Override
    public void delete(Long id) {
        var entity = pedidosRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + id));
        pedidosRepository.delete(entity);
    }

    private PedidosResponse convertirEntidadAResponse(PedidosEntity entidad) {
        var response = new PedidosResponse();
        BeanUtils.copyProperties(entidad, response);
        
        if (entidad.getUsuario() != null) {
            response.setUsuarioDni(entidad.getUsuario().getDni());
        }

        if (entidad.getItemsPedido() != null) {
            List<ItemsPedidosResponse> itemsDto = entidad.getItemsPedido().stream().map(item -> {
                ItemsPedidosResponse dto = new ItemsPedidosResponse();
                dto.setCantidad(item.getCantidad());
                dto.setPrecioUnitario(item.getPrecioUnitario());
                if (item.getProducto() != null) {
                    dto.setIdProducto(item.getProducto().getId());
                    dto.setNombreProducto(item.getProducto().getNombre());
                    dto.setDescripcionProducto(item.getProducto().getDescripcion());
                }
                return dto;
            }).collect(Collectors.toList());
            response.setItems(itemsDto);
        } else {
            response.setItems(Collections.emptyList());
        }
        return response;
    }
}