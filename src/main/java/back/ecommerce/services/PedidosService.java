package back.ecommerce.services;

import back.ecommerce.dtos.PedidosRequest;
import back.ecommerce.dtos.PedidosResponse;

public interface PedidosService {

    //Crear
    PedidosResponse create(PedidosRequest pedido); 

    //Obtener por id
    PedidosResponse readById(Long id);

    //Actualizar
    PedidosResponse update(Long id, PedidosRequest pedido);
    
    //Eliminar
    void delete(Long id);

}
