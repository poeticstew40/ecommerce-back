package back.ecommerce.services;

import java.util.List;

import back.ecommerce.dtos.PedidosRequest;
import back.ecommerce.dtos.PedidosResponse;

public interface PedidosService {

    // Crear pedido EN una tienda específica
    PedidosResponse create(String nombreTienda, PedidosRequest request);

    // Ver pedidos DE una tienda (para el vendedor)
    List<PedidosResponse> readAllByTienda(String nombreTienda);

    // Ver pedidos de un USUARIO en una tienda específica
    List<PedidosResponse> findByUsuarioDni(String nombreTienda, Long dni);

    // Métodos por ID (igual que siempre)
    PedidosResponse readById(Long id);
    PedidosResponse update(Long id, PedidosRequest request);
    void delete(Long id);
}