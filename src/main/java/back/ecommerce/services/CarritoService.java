package back.ecommerce.services;

import java.util.List;
import back.ecommerce.dtos.CarritoRequest;
import back.ecommerce.dtos.CarritoResponse;

public interface CarritoService {
    CarritoResponse agregarProducto(String nombreTienda, CarritoRequest request);
    List<CarritoResponse> obtenerCarrito(Long usuarioDni);
    void eliminarItem(Long idItem);
    void vaciarCarrito(Long usuarioDni);
}