package back.ecommerce.controllers;

import java.net.URI;

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

import back.ecommerce.dtos.ProductosRequest;
import back.ecommerce.dtos.ProductosResponse;
import back.ecommerce.services.ProductosService;
import lombok.AllArgsConstructor;


@RestController// use to expose RESTFULL
@RequestMapping(path = "productos")//wat to get this controller
@CrossOrigin(origins = "*") // Permitir solicitudes desde cualquier origen
@AllArgsConstructor
public class ProductosController {

    private final ProductosService productosService;

    //@GetMapping(path = "{nombre}")//use to get data
    //public ResponseEntity<ProductosResponse> getProductosByName(@PathVariable String nombre) {
    //     return ResponseEntity.ok(this.productosService.readByName(nombre));
    //}
    @GetMapping(path = "{id}")//use to get data
    public ResponseEntity<ProductosResponse> getProductosById (@PathVariable Long id) {
         return ResponseEntity.ok(this.productosService.readById(id));
    }

    @PostMapping
    public ResponseEntity<?> postProductos(@RequestBody ProductosRequest request){ 
        
    final var producto = this.productosService.create(request);

    URI location = ServletUriComponentsBuilder
        .fromCurrentRequest() // Toma la URL base actual (ej: http://localhost:8080/ecommerce/productos)
        .path("/{id}") // Agrega el segmento /ID
        .buildAndExpand(producto.getId()) // Sustituye {id} por el valor real
        .toUri();
    return ResponseEntity
        .created(location) // <- URI COMPLETA aquí
        .body(producto); // <- Incluir el recurso creado en el cuerpo es útil
    }

    @PatchMapping(path = "{id}")
    public ResponseEntity <ProductosResponse> updateProductos(
        @PathVariable Long id,
        @RequestBody ProductosRequest request
    ){
        
        return ResponseEntity.ok(this.productosService.update(id, request));
    }

    @DeleteMapping(path = "{id}")
    public ResponseEntity<Void> deleteProductos(@PathVariable Long id){
        this.productosService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
}
