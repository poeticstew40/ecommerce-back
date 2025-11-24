package back.ecommerce.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import back.ecommerce.dtos.CarritoRequest;
import back.ecommerce.dtos.CarritoResponse;
import back.ecommerce.entities.ItemCarritoEntity;
import back.ecommerce.repositories.CarritoRepository;
import back.ecommerce.repositories.ProductosRepository;
import back.ecommerce.repositories.UsuariosRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CarritoServiceImpl implements CarritoService {

    private final CarritoRepository carritoRepository;
    private final ProductosRepository productosRepository;
    private final UsuariosRepository usuariosRepository;

    @Override
    public CarritoResponse agregarProducto(String nombreTienda, CarritoRequest request) {
        
        // 1. Buscar Usuario
        var usuario = usuariosRepository.findById(request.getUsuarioDni())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con DNI: " + request.getUsuarioDni()));

        // 2. Buscar Producto
        var producto = productosRepository.findById(request.getProductoId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + request.getProductoId()));

        // 3. ✅ VALIDACIÓN MULTITIENDA
        if (!producto.getTienda().getNombreUrl().equals(nombreTienda)) {
            throw new IllegalArgumentException("Error de Seguridad: El producto '" + producto.getNombre() + 
                                               "' pertenece a la tienda '" + producto.getTienda().getNombreUrl() + 
                                               "' y no a '" + nombreTienda + "'.");
        }

        // 4. Validar Stock
        if (producto.getStock() < request.getCantidad()) {
            throw new IllegalArgumentException("No hay suficiente stock. Disponible: " + producto.getStock());
        }

        // 5. Lógica de Agregar o Sumar
        var itemExistente = carritoRepository.findByUsuarioDniAndProductoId(request.getUsuarioDni(), request.getProductoId());

        ItemCarritoEntity itemGuardado;

        if (itemExistente.isPresent()) {
            // Si ya existe, actualizamos la cantidad
            var item = itemExistente.get();
            item.setCantidad(item.getCantidad() + request.getCantidad());
            
            // Re-validamos stock con la nueva cantidad total
            if (producto.getStock() < item.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente para la cantidad total acumulada.");
            }
            
            itemGuardado = carritoRepository.save(item);
        } else {
            // Si no existe, creamos uno nuevo
            var nuevoItem = new ItemCarritoEntity();
            nuevoItem.setUsuario(usuario);
            nuevoItem.setProducto(producto);
            nuevoItem.setCantidad(request.getCantidad());
            itemGuardado = carritoRepository.save(nuevoItem);
        }

        return convertirAResponse(itemGuardado);
    }

    @Override
    public List<CarritoResponse> obtenerCarrito(Long usuarioDni) {
        return carritoRepository.findByUsuarioDni(usuarioDni).stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarItem(Long idItem) {
        if (!carritoRepository.existsById(idItem)) {
            throw new IllegalArgumentException("El item del carrito con ID " + idItem + " no existe.");
        }
        carritoRepository.deleteById(idItem);
    }

    @Override
    public void vaciarCarrito(Long usuarioDni) {
        carritoRepository.deleteByUsuarioDni(usuarioDni);
    }

    private CarritoResponse convertirAResponse(ItemCarritoEntity entity) {
        var producto = entity.getProducto();
        return CarritoResponse.builder()
                .idItem(entity.getId())
                .productoId(producto.getId())
                .nombreProducto(producto.getNombre())
                .imagenProducto(producto.getImagen())
                .precioUnitario(producto.getPrecio())
                .cantidad(entity.getCantidad())
                .subtotal(producto.getPrecio() * entity.getCantidad())
                .tiendaId(producto.getTienda().getId())
                .nombreTienda(producto.getTienda().getNombreFantasia())
                .build();
    }
}