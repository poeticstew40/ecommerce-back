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
    public String toggleFavorito(FavoritoRequest request) {
        // Si ya existe, lo borramos (Dislike)
        if (favoritoRepository.existsByUsuarioDniAndProductoId(request.getUsuarioDni(), request.getProductoId())) {
            favoritoRepository.deleteByUsuarioDniAndProductoId(request.getUsuarioDni(), request.getProductoId());
            return "Producto eliminado de favoritos";
        }

        // Si no existe, lo creamos (Like)
        var usuario = usuariosRepository.findById(request.getUsuarioDni())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        var producto = productosRepository.findById(request.getProductoId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        var favorito = new FavoritoEntity();
        favorito.setUsuario(usuario);
        favorito.setProducto(producto);
        
        favoritoRepository.save(favorito);
        return "Producto agregado a favoritos";
    }

    @Override
    public List<FavoritoResponse> obtenerFavoritos(Long usuarioDni) {
        return favoritoRepository.findByUsuarioDni(usuarioDni).stream()
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