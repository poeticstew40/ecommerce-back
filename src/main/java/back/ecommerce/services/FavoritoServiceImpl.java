package back.ecommerce.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import back.ecommerce.dtos.FavoritoRequest;
import back.ecommerce.dtos.FavoritoResponse;
import back.ecommerce.entities.FavoritoEntity;
import back.ecommerce.repositories.FavoritoRepository;
import back.ecommerce.repositories.ProductosRepository;
import back.ecommerce.repositories.UsuariosRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class FavoritoServiceImpl implements FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final UsuariosRepository usuariosRepository;
    private final ProductosRepository productosRepository;

    @Override
    public String toggleFavorito(String nombreTienda, FavoritoRequest request) {
        
        var producto = productosRepository.findById(request.getProductoId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + request.getProductoId()));

        // Validación: El producto debe ser de la tienda que se está navegando
        if (!producto.getTienda().getNombreUrl().equals(nombreTienda)) {
            throw new IllegalArgumentException("Error de Seguridad: El producto '" + producto.getNombre() + 
                                               "' pertenece a la tienda '" + producto.getTienda().getNombreUrl() + 
                                               "' y no a '" + nombreTienda + "'.");
        }

        if (favoritoRepository.existsByUsuarioDniAndProductoId(request.getUsuarioDni(), request.getProductoId())) {
            favoritoRepository.deleteByUsuarioDniAndProductoId(request.getUsuarioDni(), request.getProductoId());
            return "Producto eliminado de favoritos";
        }

        var usuario = usuariosRepository.findById(request.getUsuarioDni())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con DNI: " + request.getUsuarioDni()));

        var favorito = new FavoritoEntity();
        favorito.setUsuario(usuario);
        favorito.setProducto(producto);
        
        favoritoRepository.save(favorito);
        return "Producto agregado a favoritos";
    }

    @Override
    public List<FavoritoResponse> obtenerFavoritos(String nombreTienda, Long usuarioDni) {
        
        return favoritoRepository.findByUsuarioDniAndProducto_Tienda_NombreUrl(usuarioDni, nombreTienda)
                .stream()
                .map(fav -> FavoritoResponse.builder()
                        .id(fav.getId())
                        .productoId(fav.getProducto().getId())
                        .nombreProducto(fav.getProducto().getNombre())
                        .imagen(fav.getProducto().getImagen())
                        .precio(fav.getProducto().getPrecio())
                        .build())
                .collect(Collectors.toList());
    }
}