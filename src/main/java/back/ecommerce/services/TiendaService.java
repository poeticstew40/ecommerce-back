package back.ecommerce.services;

import back.ecommerce.dtos.TiendaRequest;
import back.ecommerce.dtos.TiendaResponse;

public interface TiendaService {
    
    TiendaResponse create(TiendaRequest request);
    
    TiendaResponse readByNombreUrl(String nombreUrl);
    
    // Podés agregar update y delete a futuro
}