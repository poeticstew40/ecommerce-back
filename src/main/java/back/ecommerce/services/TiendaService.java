package back.ecommerce.services;

import org.springframework.web.multipart.MultipartFile;
import back.ecommerce.dtos.TiendaRequest;
import back.ecommerce.dtos.TiendaResponse;

public interface TiendaService {
    TiendaResponse create(TiendaRequest request, MultipartFile file);
    TiendaResponse readByNombreUrl(String nombreUrl);
    TiendaResponse readByVendedorDni(Long dni);
    TiendaResponse update(String nombreUrl, TiendaRequest request, MultipartFile file);
    void delete(String nombreUrl);
}