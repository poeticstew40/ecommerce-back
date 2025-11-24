package back.ecommerce.services;

import java.util.List;

import back.ecommerce.dtos.DireccionRequest;
import back.ecommerce.dtos.DireccionResponse;

public interface DireccionService {
    DireccionResponse create(DireccionRequest request);
    List<DireccionResponse> readAllByUsuario(Long dni);
    DireccionResponse readById(Long id);
    void delete(Long id);
}