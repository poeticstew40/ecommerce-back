package back.ecommerce.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import back.ecommerce.dtos.DireccionRequest;
import back.ecommerce.dtos.DireccionResponse;
import back.ecommerce.services.DireccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios/direcciones")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DireccionController {

    private final DireccionService direccionService;

    @PostMapping
    public ResponseEntity<DireccionResponse> agregarDireccion(@Valid @RequestBody DireccionRequest request) {
        return ResponseEntity.ok(direccionService.create(request));
    }

    @GetMapping("/{dni}")
    public ResponseEntity<List<DireccionResponse>> listarDirecciones(@PathVariable Long dni) {
        return ResponseEntity.ok(direccionService.readAllByUsuario(dni));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDireccion(@PathVariable Long id) {
        direccionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}