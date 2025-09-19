package back.ecommerce.services;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import back.ecommerce.dtos.PedidosRequest;
import back.ecommerce.dtos.PedidosResponse;
import back.ecommerce.entities.PedidosEntity;
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

    @Override
    public PedidosResponse create(PedidosRequest pedido) {
        final var entity = new PedidosEntity(); //create object(entity) to persist in database
        BeanUtils.copyProperties(pedido, entity); // copy properties from argument(pedido) in entity

        final var usuario = usuariosRepository.findById(pedido.getUsuarioId()) // search user correspondig to pedido
            .orElseThrow();

        entity.setFechaPedido(LocalDateTime.now());// set current date
        entity.setUsuario(usuario);// create relationship between pedido and usuarios
        entity.setItemsPedido(new ArrayList<>());// set empty list

        var pedidoCreated = this.pedidosRepository.save(entity);//upsert id exist id update else insert

        final var response = new PedidosResponse();//create dto for response

        BeanUtils.copyProperties(pedidoCreated, response);//copy properties from entity(pedidoCreated) to response
        return response;
    }

    @Override
    public PedidosResponse readById(Long id) {
        throw new UnsupportedOperationException("Unimplemented method 'readById'");
    }

    @Override
    public PedidosResponse update(Long id, PedidosRequest pedido) {
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public void delete(Long id) {
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

}
