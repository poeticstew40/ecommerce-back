package back.ecommerce.services;

import java.util.List;

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

    //lista de pedidos de un usuario por dni
    List<PedidosResponse> findByUsuarioDni(Long dni);

}
