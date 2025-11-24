package back.ecommerce.services;

import java.util.List;

import back.ecommerce.dtos.CarritoRequest;
import back.ecommerce.dtos.CarritoResponse;

public interface CarritoService {
    
    // Agregar producto o sumar cantidad si ya existe
    CarritoResponse agregarProducto(CarritoRequest request);
    
    // Ver el carrito completo del usuario
    List<CarritoResponse> obtenerCarrito(Long usuarioDni);
    
    // Eliminar un item específico
    void eliminarItem(Long idItem);
    
    // Vaciar todo el carrito del usuario
    void vaciarCarrito(Long usuarioDni);
}