package back.ecommerce.services;

import back.ecommerce.dtos.ItemsPedidosRequest;
import back.ecommerce.dtos.ItemsPedidosResponse;

public interface ItemsPedidosService {

    //Crear
    ItemsPedidosResponse create(ItemsPedidosRequest itemsPedidos);
    //Obtener por id
    ItemsPedidosResponse readById(Long id);
    //Actualizar
    ItemsPedidosResponse update(Long id, ItemsPedidosRequest itemsPedidos);
    //Eliminar
    void delete(Long id);
    
}