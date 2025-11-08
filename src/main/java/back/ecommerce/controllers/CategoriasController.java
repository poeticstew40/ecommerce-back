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
@RequestMapping(path = "categorias")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class CategoriasController {

    private final CategoriasService categoriasService;
    
    @GetMapping(path = "{id}")
    public ResponseEntity<CategoriasResponse> getCategoriasById (@PathVariable Long id) {
         return ResponseEntity.ok(this.categoriasService.readById(id));
    }

    @GetMapping
    public ResponseEntity<List<CategoriasResponse>> getAll() {
        
        final List<CategoriasResponse> categorias = this.categoriasService.readAll();
        
        
        return ResponseEntity.ok(categorias);
    }
    

    @PostMapping
    public ResponseEntity<CategoriasResponse> postCategorias(@RequestBody CategoriasRequest request){ 
        
        final var categoria = this.categoriasService.create(request);
    
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest() // Toma la URL base actual
            .path("/{id}") // Agrega el segmento /ID
            .buildAndExpand(categoria.getId()) // Sustituye {id} por el valor real
            .toUri();
        return ResponseEntity
            .created(location)
            .body(categoria);
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
