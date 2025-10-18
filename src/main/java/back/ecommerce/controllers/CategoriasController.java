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
import lombok.AllArgsConstructor;


@RestController
@RequestMapping(path = "categorias")//use to get this controller
@CrossOrigin(origins = "*") // Permitir solicitudes desde cualquier origen
@AllArgsConstructor
public class CategoriasController {

    private final CategoriasService categoriasService;
    
    @GetMapping(path = "{id}")//use to get data
    public ResponseEntity<CategoriasResponse> getCategoriasById (@PathVariable Long id) {
         return ResponseEntity.ok(this.categoriasService.readById(id));
    }

    @GetMapping
    public ResponseEntity<List<CategoriasResponse>> getAll() {
        // 1. Llama al servicio para obtener la lista completa de DTOs.
        final List<CategoriasResponse> categorias = this.categoriasService.readAll();
        
        // 2. Devuelve la lista en el cuerpo de la respuesta con un estado 200 OK.
        return ResponseEntity.ok(categorias);
    }
    

    @PostMapping
    public ResponseEntity<?> postCategorias(@RequestBody CategoriasRequest request){ 
        
        final var categoria = this.categoriasService.create(request);
    
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest() // Toma la URL base actual (ej: http://localhost:8080/ecommerce/categorias)
            .path("/{id}") // Agrega el segmento /ID
            .buildAndExpand(categoria.getId()) // Sustituye {id} por el valor real
            .toUri();
        return ResponseEntity
            .created(location) // <- URI COMPLETA aquí
            .body(categoria); // <- Incluir el recurso creado en el cuerpo es útil
        }

    @PatchMapping(path = "{id}")
    public ResponseEntity<CategoriasResponse> updateCategoria(
        @PathVariable Long id,
        @RequestBody CategoriasRequest request    
    ) {
        return ResponseEntity.ok(this.categoriasService.update(id, request));
    }

    @DeleteMapping(path = "{id}")
    public ResponseEntity<Void> deleteCategoria(@PathVariable Long id) {
        this.categoriasService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
