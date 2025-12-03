package back.ecommerce.services;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import back.ecommerce.dtos.TiendaRequest;
import back.ecommerce.dtos.TiendaResponse;

public interface TiendaService {
    TiendaResponse create(TiendaRequest request, MultipartFile logoFile, List<MultipartFile> bannerFiles);
    TiendaResponse readByNombreUrl(String nombreUrl);
    TiendaResponse readByVendedorDni(Long dni);
    TiendaResponse update(String nombreUrl, TiendaRequest request, MultipartFile logoFile, List<MultipartFile> bannerFiles);
    void delete(String nombreUrl);
}