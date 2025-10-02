package back.ecommerce.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import back.ecommerce.dtos.ItemsPedidosResponse;
import back.ecommerce.dtos.PedidosRequest;
import back.ecommerce.dtos.PedidosResponse;
import back.ecommerce.entities.PedidosEntity;
import back.ecommerce.repositories.ItemsPedidosRepository;
import back.ecommerce.repositories.PedidosRepository;
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
    private final ItemsPedidosRepository itemsPedidosRepository;

    @Override
    public PedidosResponse create(PedidosRequest pedido) {
        final var entity = new PedidosEntity(); //create object(entity) to persist in database
        BeanUtils.copyProperties(pedido, entity); // copy properties from argument(pedido) in entity

        final var usuario = usuariosRepository.findById(pedido.getUsuarioDni()) // search user correspondig to pedido
            .orElseThrow();

        entity.setFechaPedido(LocalDateTime.now());// set current date
        entity.setUsuario(usuario);// create relationship between pedido and usuarios
        entity.setItemsPedido(new ArrayList<>());// set empty list

        var pedidoCreated = this.pedidosRepository.save(entity);//upsert id exist id update else insert

        final var response = new PedidosResponse();//create dto for response

        BeanUtils.copyProperties(pedidoCreated, response);//copy properties from entity(pedidoCreated) to response
        response.setUsuarioDni(pedidoCreated.getUsuario().getDni());

        if(pedidoCreated.getUsuario() != null) {
            response.setUsuarioDni(pedidoCreated.getUsuario().getDni());
        }

            if (response.getItems() == null) {
        response.setItems(new ArrayList<>());
        }
        // Si el estado es null, inicialízalo
        if (response.getEstado() == null) {
            response.setEstado("PENDIENTE");
        }
        return response;
    }

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
                    .build()
            )
            .toList(); // convert to list

        response.setItems(itemsPedidosResponse);// set list of itemsPedidos

        return response;
    }

    @Override
    public PedidosResponse update(Long id, PedidosRequest pedido) {
        final var entityFromDB = this.pedidosRepository.findById(id) 
            .orElseThrow(() -> new IllegalArgumentException("No existe el pedido con id: " + id)); // Find by id and handle errors


        entityFromDB.setTotal(pedido.getTotal());//update fields from param pedido

        var pedidoCreated = this.pedidosRepository.save(entityFromDB);//upsert id exist id update else insert

        final var response = new PedidosResponse();//create dto for response

        BeanUtils.copyProperties(pedidoCreated, response);//copy properties from entity(pedidoCreated) to response

        return response;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        
        if(this.pedidosRepository.existsById(id)) {
            log.info("Eliminando pedido con id: {}", id);

            this.itemsPedidosRepository.deleteById(id);

            this.pedidosRepository.deleteById(id);
        } else {
            log.error("No existe el pedido con id: {}", id);
            throw new IllegalArgumentException("No se puede eliminar porque no existe el pedido con id: " + id);
        }
    }

}
