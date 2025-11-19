package back.ecommerce.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import back.ecommerce.dtos.CategoriasRequest;
import back.ecommerce.dtos.CategoriasResponse;
import back.ecommerce.services.CategoriasService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/tienda/{nombreTienda}/categorias") // 👈 Ruta base dinámica
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class CategoriasController {

    private final CategoriasService categoriasService;

    @GetMapping
    public ResponseEntity<List<CategoriasResponse>> getAllByTienda(@PathVariable String nombreTienda) {
        return ResponseEntity.ok(this.categoriasService.readAllByTienda(nombreTienda));
    }

    @PostMapping
    public ResponseEntity<CategoriasResponse> postCategorias(
            @PathVariable String nombreTienda,
            @Valid @RequestBody CategoriasRequest request) {

        final var categoria = this.categoriasService.create(nombreTienda, request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/tienda/{nombreTienda}/categorias/{id}")
                .buildAndExpand(nombreTienda, categoria.getId())
                .toUri();

        return ResponseEntity.created(location).body(categoria);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriasResponse> getCategoriasById(
            @PathVariable String nombreTienda,
            @PathVariable Long id) {
        return ResponseEntity.ok(this.categoriasService.readById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CategoriasResponse> updateCategoria(
            @PathVariable String nombreTienda,
            @PathVariable Long id,
            @RequestBody CategoriasRequest request) {
        return ResponseEntity.ok(this.categoriasService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoria(
            @PathVariable String nombreTienda,
            @PathVariable Long id) {
        this.categoriasService.delete(id);
        return ResponseEntity.noContent().build();
    }
}