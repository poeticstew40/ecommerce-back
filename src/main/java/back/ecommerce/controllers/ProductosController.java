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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import back.ecommerce.dtos.ProductosRequest;
import back.ecommerce.dtos.ProductosResponse;
import back.ecommerce.services.ProductosService;
import lombok.AllArgsConstructor;



@RestController
@RequestMapping(path = "productos")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class ProductosController {

    private final ProductosService productosService;

    @GetMapping(path = "{id}")
    public ResponseEntity<ProductosResponse> getProductosById (@PathVariable Long id) {
         return ResponseEntity.ok(this.productosService.readById(id));
    }

    @PostMapping
    public ResponseEntity<ProductosResponse> postProductos(@RequestBody ProductosRequest request){ 
        
    final var producto = this.productosService.create(request);

    URI location = ServletUriComponentsBuilder
        .fromCurrentRequest() // Toma la URL base actual (ej: http://localhost:8080/ecommerce/productos)
        .path("/{id}") // Agrega el segmento /ID
        .buildAndExpand(producto.getId()) // Sustituye {id} por el valor real
        .toUri();
    return ResponseEntity
        .created(location)
        .body(producto);
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

    @GetMapping()
    public ResponseEntity<List<ProductosResponse>>getAll() {
        final List<ProductosResponse> productos = this.productosService.readAll();

        return ResponseEntity.ok(productos);
    }

    @GetMapping(path = "/buscar")
    public ResponseEntity<List<ProductosResponse>> buscarProductos(
            @RequestParam("q") String terminoBusqueda) {
        
        return ResponseEntity.ok(this.productosService.buscarPorNombre(terminoBusqueda));
    }

    @GetMapping(path = "/categoria/{id}")
    public ResponseEntity<List<ProductosResponse>> obtenerProductosPorCategoria(
            @PathVariable Long id) {
        
        return ResponseEntity.ok(this.productosService.buscarPorCategoria(id));
    }
    
}
