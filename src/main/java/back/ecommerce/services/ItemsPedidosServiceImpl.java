package back.ecommerce.services;

import back.ecommerce.dtos.ItemsPedidosRequest;
import back.ecommerce.dtos.ItemsPedidosResponse;
import back.ecommerce.repositories.ItemsPedidosRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class ItemsPedidosServiceImpl implements  ItemsPedidosService{

    private final ItemsPedidosRepository itemsPedidosRepository;

    @Override
    public ItemsPedidosResponse create(ItemsPedidosRequest itemsPedidos) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ItemsPedidosResponse readById(Long id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ItemsPedidosResponse update(Long id, ItemsPedidosRequest itemsPedidos) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void delete(Long id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
