package back.ecommerce.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import back.ecommerce.dtos.ItemsPedidosResponse;
import back.ecommerce.dtos.PedidosRequest;
import back.ecommerce.dtos.PedidosResponse;
import back.ecommerce.entities.ItemsPedidosEntity;
import back.ecommerce.entities.PedidosEntity;
import back.ecommerce.repositories.PedidosRepository;
import back.ecommerce.repositories.ProductosRepository;
import back.ecommerce.repositories.UsuariosRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class PedidosServiceImpl implements PedidosService {

    private final PedidosRepository pedidosRepository;
    private final UsuariosRepository usuariosRepository;
    private final ProductosRepository productosRepository;

    @Override
    @Transactional
    public PedidosResponse create(PedidosRequest pedidoRequest) {

        var pedidoEntity = new PedidosEntity();
        var usuario = usuariosRepository.findById(pedidoRequest.getUsuarioDni())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con DNI: " + pedidoRequest.getUsuarioDni()));

        pedidoEntity.setUsuario(usuario);
        pedidoEntity.setFechaPedido(LocalDateTime.now());
        pedidoEntity.setEstado("PENDIENTE");

        BigDecimal totalCalculado = BigDecimal.ZERO;

        if (pedidoRequest.getItems() != null && !pedidoRequest.getItems().isEmpty()) {
            for (var itemReq : pedidoRequest.getItems()) {
                var producto = productosRepository.findById(itemReq.getIdProducto())
                        .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con id: " + itemReq.getIdProducto()));

                //verificar y descontar stock por hacer

                var itemEntity = new ItemsPedidosEntity();
                itemEntity.setCantidad(itemReq.getCantidad());
                itemEntity.setProducto(producto);
                itemEntity.setPrecioUnitario(producto.getPrecio()); 

                pedidoEntity.addItemPedido(itemEntity);

                BigDecimal cantidad = new BigDecimal(itemReq.getCantidad());
                BigDecimal precioReal = BigDecimal.valueOf(producto.getPrecio());

                BigDecimal subtotal = precioReal.multiply(cantidad);
                totalCalculado = totalCalculado.add(subtotal);
            }
        }

        pedidoEntity.setTotal(totalCalculado.doubleValue());

        var pedidoGuardado = pedidosRepository.save(pedidoEntity);

        var response = new PedidosResponse();
        BeanUtils.copyProperties(pedidoGuardado, response);
        response.setUsuarioDni(pedidoGuardado.getUsuario().getDni());

        var itemsResponse = pedidoGuardado.getItemsPedido().stream().map(itemE ->
            ItemsPedidosResponse.builder()
                .cantidad(itemE.getCantidad())
                .precioUnitario(itemE.getPrecioUnitario())
                .nombreProducto(itemE.getProducto().getNombre())
                .descripcionProducto(itemE.getProducto().getDescripcion())
                .idProducto(itemE.getProducto().getId())
                .build()
        ).toList();

        response.setItems(itemsResponse);

        return response;
    }


    @Override
    public PedidosResponse readById(Long id) {

        final var entityResponse = this.pedidosRepository.findById(id) 
            .orElseThrow(() -> new IllegalArgumentException("No existe el pedido con id: " + id)); 

        final var response = new PedidosResponse();


        BeanUtils.copyProperties(entityResponse, response);

        if(entityResponse.getUsuario() != null) {
        response.setUsuarioDni(entityResponse.getUsuario().getDni());
        }

        final List<ItemsPedidosResponse> itemsPedidosResponse = entityResponse.getItemsPedido()
            .stream()
            .map(itemsE -> 
                ItemsPedidosResponse
                    .builder()
                    .cantidad(itemsE.getCantidad())
                    .precioUnitario(itemsE.getPrecioUnitario())
                    .nombreProducto(itemsE.getProducto().getNombre())
                    .descripcionProducto(itemsE.getProducto().getDescripcion())
                    .idProducto(itemsE.getProducto().getId())
                    .build()
            )
            .toList();

        response.setItems(itemsPedidosResponse);

        return response;
    }


    @Override
    public PedidosResponse update(Long id, PedidosRequest pedidoRequest) {
        final var entityFromDB = this.pedidosRepository.findById(id) 
                .orElseThrow(() -> new IllegalArgumentException("No existe el pedido con id: " + id)); 
        
        if (pedidoRequest.getEstado() != null && !pedidoRequest.getEstado().isBlank()) {
            entityFromDB.setEstado(pedidoRequest.getEstado());
        }

        var pedidoActualizado = this.pedidosRepository.save(entityFromDB);

        final var response = new PedidosResponse();
        BeanUtils.copyProperties(pedidoActualizado, response);

        if (pedidoActualizado.getUsuario() != null) {
            response.setUsuarioDni(pedidoActualizado.getUsuario().getDni());
        }

        if (pedidoActualizado.getItemsPedido() != null && !pedidoActualizado.getItemsPedido().isEmpty()) {
            
            List<ItemsPedidosResponse> itemsResponseList = pedidoActualizado.getItemsPedido().stream()
                .map(itemEntity -> {
                    ItemsPedidosResponse itemDto = new ItemsPedidosResponse();
                    
                    itemDto.setCantidad(itemEntity.getCantidad());
                    itemDto.setPrecioUnitario(itemEntity.getPrecioUnitario());
                    
                    if (itemEntity.getProducto() != null) {

                    itemDto.setIdProducto(itemEntity.getProducto().getId()); 
                    itemDto.setNombreProducto(itemEntity.getProducto().getNombre()); 
                    itemDto.setDescripcionProducto(itemEntity.getProducto().getDescripcion());
                    
                    }
                    return itemDto;
                }).collect(Collectors.toList());
            
            response.setItems(itemsResponseList);

        } else {
            response.setItems(Collections.emptyList());
        }
        
        return response;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        var pedido = this.pedidosRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("No existe el pedido con id: " + id));
     
            log.info("Eliminando pedido con id: {}", id);

            pedidosRepository.delete(pedido);
   
    }

    @Override
    public List<PedidosResponse> findByUsuarioDni(Long dni) {
        List<PedidosEntity> pedidosEntities = this.pedidosRepository.findByUsuarioDni(dni);

        return pedidosEntities.stream()
            .map(pedidoEntity -> {
                PedidosResponse response = new PedidosResponse();
                BeanUtils.copyProperties(pedidoEntity, response);
                response.setUsuarioDni(pedidoEntity.getUsuario().getDni());

                List<ItemsPedidosResponse> itemsResponse = pedidoEntity.getItemsPedido().stream()
                    .map(itemE ->
                        ItemsPedidosResponse.builder()
                            .cantidad(itemE.getCantidad())
                            .precioUnitario(itemE.getPrecioUnitario())
                            .nombreProducto(itemE.getProducto().getNombre())
                            .descripcionProducto(itemE.getProducto().getDescripcion())
                            .idProducto(itemE.getProducto().getId())
                            .build()
                    ).toList();

                response.setItems(itemsResponse);

                return response;
            })
            .toList();
    }


}
