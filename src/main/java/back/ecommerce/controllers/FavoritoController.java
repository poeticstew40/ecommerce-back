package back.ecommerce.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import back.ecommerce.dtos.FavoritoRequest;
import back.ecommerce.dtos.FavoritoResponse;
import back.ecommerce.services.FavoritoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tiendas/{nombreTienda}/favoritos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class FavoritoController {

    private final FavoritoService favoritoService;

    @PostMapping("/toggle")
    public ResponseEntity<Map<String, String>> toggleFavorito(
            @PathVariable String nombreTienda, // ✅ Capturamos el slug de la tienda
            @Valid @RequestBody FavoritoRequest request) {
        
        // Pasamos el nombreTienda al servicio para que valide
        String mensaje = favoritoService.toggleFavorito(nombreTienda, request);
        return ResponseEntity.ok(Map.of("mensaje", mensaje));
    }

    @GetMapping("/{usuarioDni}")
    public ResponseEntity<List<FavoritoResponse>> listarFavoritos(
            @PathVariable String nombreTienda, 
            @PathVariable Long usuarioDni) {
        
        return ResponseEntity.ok(favoritoService.obtenerFavoritos(nombreTienda, usuarioDni));
    }
}