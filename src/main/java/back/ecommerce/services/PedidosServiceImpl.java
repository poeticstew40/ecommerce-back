package back.ecommerce.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service; // 👈 Importante
import org.springframework.transaction.annotation.Transactional;

import back.ecommerce.dtos.ItemsPedidosResponse;
import back.ecommerce.dtos.PedidosRequest;
import back.ecommerce.dtos.PedidosResponse;
import back.ecommerce.entities.ItemsPedidosEntity;
import back.ecommerce.entities.PedidosEntity;
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
    private final TiendaRepository tiendaRepository; // 👈 Nuevo repo

    @Override
    public PedidosResponse create(String nombreTienda, PedidosRequest pedidoRequest) {
        
        // 1. Buscamos la TIENDA
        var tienda = tiendaRepository.findByNombreUrl(nombreTienda)
                .orElseThrow(() -> new IllegalArgumentException("Tienda no encontrada: " + nombreTienda));

        // 2. Buscamos el Usuario
        var usuario = usuariosRepository.findById(pedidoRequest.getUsuarioDni())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con DNI: " + pedidoRequest.getUsuarioDni()));

        // 3. Armamos el Pedido
        var pedidoEntity = new PedidosEntity();
        pedidoEntity.setUsuario(usuario);
        pedidoEntity.setTienda(tienda); // 👈 ASIGNAMOS LA TIENDA
        pedidoEntity.setFechaPedido(LocalDateTime.now());
        pedidoEntity.setEstado("PENDIENTE");
        pedidoEntity.setItemsPedido(new ArrayList<>());

        BigDecimal totalCalculado = BigDecimal.ZERO;

        // 4. Procesamos Items y calculamos total
        if (pedidoRequest.getItems() != null && !pedidoRequest.getItems().isEmpty()) {
            for (var itemReq : pedidoRequest.getItems()) {
                var producto = productosRepository.findById(itemReq.getIdProducto())
                        .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con id: " + itemReq.getIdProducto()));

                // Validar que el producto pertenezca a la misma tienda (Opcional pero recomendado)
                // if (!producto.getTienda().getId().equals(tienda.getId())) { ... error ... }

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
        }

        pedidoEntity.setTotal(totalCalculado.doubleValue());
        var pedidoGuardado = pedidosRepository.save(pedidoEntity);

        return convertirEntidadAResponse(pedidoGuardado);
    }

    @Override
    public List<PedidosResponse> readAllByTienda(String nombreTienda) {
        // Usamos el repo para filtrar por tienda
        return pedidosRepository.findByTiendaNombreUrl(nombreTienda).stream()
                .map(this::convertirEntidadAResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PedidosResponse> findByUsuarioDni(String nombreTienda, Long dni) {
        // Filtramos por tienda Y usuario
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

    // --- Helper ---
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