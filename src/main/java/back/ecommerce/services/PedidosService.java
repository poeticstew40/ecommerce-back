package back.ecommerce.services;

import java.util.List;
import back.ecommerce.dtos.PedidosRequest;
import back.ecommerce.dtos.PedidosResponse;

public interface PedidosService {
    PedidosResponse create(String nombreTienda, PedidosRequest request);
    List<PedidosResponse> readAllByTienda(String nombreTienda);
    List<PedidosResponse> findByUsuarioDni(String nombreTienda, Long dni);
    List<PedidosResponse> findAllByUsuarioDniGlobal(Long dni);
    PedidosResponse readById(Long id);
    PedidosResponse update(Long id, PedidosRequest request);
    void delete(Long id);
}