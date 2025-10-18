package back.ecommerce.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    //create pedido
    @Override
    public PedidosResponse create(PedidosRequest pedido) {

        var entity = new PedidosEntity();
        BeanUtils.copyProperties(pedido, entity);


        var usuario = usuariosRepository.findById(pedido.getUsuarioDni())
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con DNI: " + pedido.getUsuarioDni()));

        entity.setUsuario(usuario);
        entity.setFechaPedido(LocalDateTime.now());

        if (entity.getEstado() == null) {
            entity.setEstado("PENDIENTE");
        }

        entity.setItemsPedido(new ArrayList<>());

        // 🔽 Mapear los ítems del request
        if (pedido.getItems() != null && !pedido.getItems().isEmpty()) {
            for (var itemReq : pedido.getItems()) {
                var itemEntity = new ItemsPedidosEntity();
                itemEntity.setCantidad(itemReq.getCantidad());

                // Buscar el producto
                var producto = productosRepository.findById(itemReq.getIdProducto())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con id: " + itemReq.getIdProducto()));

                itemEntity.setProducto(producto);
                itemEntity.setPedido(entity);
                itemEntity.setPrecioUnitario(producto.getPrecio()); // 👈 usa el precio del producto

                entity.getItemsPedido().add(itemEntity);
            }
        }

        var pedidoCreated = pedidosRepository.save(entity);

        //  Construir respuesta
        var response = new PedidosResponse();
        BeanUtils.copyProperties(pedidoCreated, response);
        response.setUsuarioDni(pedidoCreated.getUsuario().getDni());

        var itemsResponse = pedidoCreated.getItemsPedido().stream().map(itemE ->
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


    //get pedido by id
    @Override
    public PedidosResponse readById(Long id) {

        final var entityResponse = this.pedidosRepository.findById(id) 
            .orElseThrow(() -> new IllegalArgumentException("No existe el pedido con id: " + id)); // Find by id and handle errors

        final var response = new PedidosResponse(); // create response object


        BeanUtils.copyProperties(entityResponse, response); // copy properties from entity

        if(entityResponse.getUsuario() != null) {
        response.setUsuarioDni(entityResponse.getUsuario().getDni());
        }

        //Get itemsPedidos response fron itemsPedidosEntity
        final List<ItemsPedidosResponse> itemsPedidosResponse = entityResponse.getItemsPedido()
            .stream()// convert to stream
            .map(itemsE ->  // transform itemsPedidosEntity to itemsPedidosResponse
                ItemsPedidosResponse
                    .builder()
                    .cantidad(itemsE.getCantidad())
                    .precioUnitario(itemsE.getPrecioUnitario())
                    .nombreProducto(itemsE.getProducto().getNombre())
                    .descripcionProducto(itemsE.getProducto().getDescripcion())
                    .idProducto(itemsE.getProducto().getId())
                    .build()
            )
            .toList(); // convert to list

        response.setItems(itemsPedidosResponse);// set list of itemsPedidos

        return response;
    }

  // Dentro de tu clase PedidosServiceImpl.java

@Override
public PedidosResponse update(Long id, PedidosRequest pedidoRequest) {
    // 1. Busca el pedido en la base de datos.
    final var entityFromDB = this.pedidosRepository.findById(id) 
            .orElseThrow(() -> new IllegalArgumentException("No existe el pedido con id: " + id)); 
    
    // 2. Lógica de actualización parcial: solo se actualiza el estado.
    if (pedidoRequest.getEstado() != null && !pedidoRequest.getEstado().isBlank()) {
        entityFromDB.setEstado(pedidoRequest.getEstado());
    }

    // 3. Guarda la entidad con su nuevo estado.
    var pedidoActualizado = this.pedidosRepository.save(entityFromDB);

    // 4. Crea el DTO de respuesta y copia las propiedades básicas.
    final var response = new PedidosResponse();
    BeanUtils.copyProperties(pedidoActualizado, response);

    // 5. Rellena la información del usuario en la respuesta.
    if (pedidoActualizado.getUsuario() != null) {
        response.setUsuarioDni(pedidoActualizado.getUsuario().getDni());
    }

    // 6. Rellena la lista detallada de items del pedido en la respuesta.
    if (pedidoActualizado.getItemsPedido() != null && !pedidoActualizado.getItemsPedido().isEmpty()) {
        
        // Separo la lógica del stream para que sea más clara
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

    //delete pedido
    @Override
    @Transactional
    public void delete(Long id) {
        var pedido = this.pedidosRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("No existe el pedido con id: " + id));
     
            log.info("Eliminando pedido con id: {}", id);

            pedidosRepository.delete(pedido);
   
    }

}
