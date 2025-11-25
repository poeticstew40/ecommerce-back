package back.ecommerce.services;

import java.util.List;

import back.ecommerce.dtos.FavoritoRequest;
import back.ecommerce.dtos.FavoritoResponse;

public interface FavoritoService {
    
    String toggleFavorito(FavoritoRequest request);
    
    List<FavoritoResponse> obtenerFavoritos(Long usuarioDni);
}