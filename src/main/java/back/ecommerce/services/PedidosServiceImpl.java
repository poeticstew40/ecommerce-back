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
import back.ecommerce.dtos.ItemsPedidosResponse;
import back.ecommerce.dtos.PedidosRequest;
import back.ecommerce.dtos.PedidosResponse;
import back.ecommerce.entities.ItemsPedidosEntity;
import back.ecommerce.entities.PedidosEntity;
import back.ecommerce.repositories.CarritoRepository;
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
    private final CarritoRepository carritoRepository;

    @Override
    public PedidosResponse create(String nombreTienda, PedidosRequest pedidoRequest) {
        
        var tienda = tiendaRepository.findByNombreUrl(nombreTienda)
                .orElseThrow(() -> new IllegalArgumentException("Tienda no encontrada: " + nombreTienda));

        var usuario = usuariosRepository.findById(pedidoRequest.getUsuarioDni())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con DNI: " + pedidoRequest.getUsuarioDni()));

        List<ItemsPedidosRequest> itemsParaProcesar = new ArrayList<>();
        boolean vieneDelCarrito = false;

        if (pedidoRequest.getItems() == null || pedidoRequest.getItems().isEmpty()) {
            var itemsCarrito = carritoRepository.findByUsuarioDni(usuario.getDni());
            if (itemsCarrito.isEmpty()) {
                throw new IllegalArgumentException("El carrito está vacío y no se enviaron items manuales.");
            }

            itemsParaProcesar = itemsCarrito.stream().map(itemCart -> {
                return ItemsPedidosRequest.builder()
                        .productoId(itemCart.getProducto().getId())
                        .cantidad(itemCart.getCantidad())
                        .build();
            }).collect(Collectors.toList());
            vieneDelCarrito = true;
        } else {
            itemsParaProcesar = pedidoRequest.getItems().stream().map(item -> {
                return ItemsPedidosRequest.builder()
                        .productoId(item.getIdProducto()) // DTO mapping adjusted
                        .cantidad(item.getCantidad())
                        .build();
            }).collect(Collectors.toList());
        }

        var pedidoEntity = new PedidosEntity();
        pedidoEntity.setUsuario(usuario);
        pedidoEntity.setTienda(tienda);
        pedidoEntity.setFechaPedido(LocalDateTime.now());
        pedidoEntity.setEstado("PENDIENTE");
        pedidoEntity.setItemsPedido(new ArrayList<>());
        
        pedidoEntity.setMetodoEnvio(pedidoRequest.getMetodoEnvio());

        if (pedidoRequest.getDireccionEnvio() == null || pedidoRequest.getDireccionEnvio().isBlank()) {
             if (usuario.getDirecciones() != null && !usuario.getDirecciones().isEmpty()) {
                 var dir = usuario.getDirecciones().get(0);
                 String direccionTexto = dir.getCalle() + " " + dir.getNumero() + ", " + 
                                         dir.getLocalidad() + " (" + dir.getProvincia() + ")";
                 pedidoEntity.setDireccionEnvio(direccionTexto);
             } else {
                 throw new IllegalArgumentException("Debes ingresar una dirección de envío o cargar una en tu perfil.");
             }
        } else {
             pedidoEntity.setDireccionEnvio(pedidoRequest.getDireccionEnvio());
        }
        
        double costoEnvio = pedidoRequest.getCostoEnvio() != null ? pedidoRequest.getCostoEnvio() : 0.0;
        if (costoEnvio < 0) {
            throw new IllegalArgumentException("El costo de envío no puede ser negativo.");
        }
        pedidoEntity.setCostoEnvio(costoEnvio);

        BigDecimal totalCalculado = BigDecimal.ZERO;

        for (var itemReq : itemsParaProcesar) {
            var producto = productosRepository.findById(itemReq.getProductoId()) 
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con id: " + itemReq.getProductoId()));

            if (!producto.getTienda().getId().equals(tienda.getId())) {
                throw new IllegalArgumentException("El producto '" + producto.getNombre() + "' no pertenece a la tienda '" + nombreTienda + "'");
            }

            if (producto.getStock() < itemReq.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente para: " + producto.getNombre() + ". Disponible: " + producto.getStock());
            }

            producto.setStock(producto.getStock() - itemReq.getCantidad());
            productosRepository.save(producto);

            var itemEntity = new ItemsPedidosEntity();
            itemEntity.setCantidad(itemReq.getCantidad());
            itemEntity.setProducto(producto);
            itemEntity.setPrecioUnitario(producto.getPrecio());
            itemEntity.setPedido(pedidoEntity);
            
            pedidoEntity.getItemsPedido().add(itemEntity);

            BigDecimal cantidad = new BigDecimal(itemReq.getCantidad());
            BigDecimal subtotal = BigDecimal.valueOf(producto.getPrecio()).multiply(cantidad);
            totalCalculado = totalCalculado.add(subtotal);
        }

        if (pedidoEntity.getCostoEnvio() > 0) {
            totalCalculado = totalCalculado.add(BigDecimal.valueOf(pedidoEntity.getCostoEnvio()));
        }

        pedidoEntity.setTotal(totalCalculado.doubleValue());
        var pedidoGuardado = pedidosRepository.save(pedidoEntity);

        if (vieneDelCarrito) {
            carritoRepository.deleteByUsuarioDni(usuario.getDni());
        }

        return convertirEntidadAResponse(pedidoGuardado);
    }
    
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
        
        // Mapear datos del usuario
        if (entidad.getUsuario() != null) {
            response.setUsuarioDni(entidad.getUsuario().getDni());
            response.setUsuarioNombre(entidad.getUsuario().getNombre()); 
            response.setUsuarioApellido(entidad.getUsuario().getApellido());
        }

        response.setDireccionEnvio(entidad.getDireccionEnvio());
        response.setMetodoEnvio(entidad.getMetodoEnvio());
        response.setCostoEnvio(entidad.getCostoEnvio());

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