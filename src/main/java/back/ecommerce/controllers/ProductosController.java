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
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/tiendas/{nombreTienda}/productos") // 👈 ¡Ruta base dinámica!
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class ProductosController {

    private final ProductosService productosService;

    // 1. Obtener TODOS los productos de ESA tienda
    @GetMapping
    public ResponseEntity<List<ProductosResponse>> getAllByTienda(
            @PathVariable String nombreTienda) { // 👈 Captura el slug
        
        return ResponseEntity.ok(this.productosService.readAllByTienda(nombreTienda));
    }

    // 2. Crear un producto en ESA tienda
    @PostMapping
    public ResponseEntity<ProductosResponse> postProductos(
            @PathVariable String nombreTienda, // 👈 Captura el slug
            @Valid @RequestBody ProductosRequest request) {

        final var producto = this.productosService.create(nombreTienda, request);

        // Ajustamos la URI de respuesta para que incluya la tienda
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath() // http://localhost:8080/ecommerce
                .path("/tienda/{nombreTienda}/productos/{id}")
                .buildAndExpand(nombreTienda, producto.getId())
                .toUri();

        return ResponseEntity.created(location).body(producto);
    }

    // 3. Buscar por nombre (dentro de la tienda)
    @GetMapping("/buscar")
    public ResponseEntity<List<ProductosResponse>> buscarProductos(
            @PathVariable String nombreTienda,
            @RequestParam("q") String termino) {

        return ResponseEntity.ok(this.productosService.buscarPorNombre(nombreTienda, termino));
    }

    // 4. Filtrar por categoría (dentro de la tienda)
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<ProductosResponse>> buscarPorCategoria(
            @PathVariable String nombreTienda,
            @PathVariable Long categoriaId) {

        return ResponseEntity.ok(this.productosService.buscarPorCategoria(nombreTienda, categoriaId));
    }

    // --- Métodos por ID (El ID es único global, pero la URL lleva la tienda por estructura) ---

    @GetMapping("/{id}")
    public ResponseEntity<ProductosResponse> getProductosById(
            @PathVariable String nombreTienda, // Se pide por la URL aunque no se use en el service readById
            @PathVariable Long id) {
        return ResponseEntity.ok(this.productosService.readById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductosResponse> updateProductos(
            @PathVariable String nombreTienda,
            @PathVariable Long id,
            @RequestBody ProductosRequest request) {
        return ResponseEntity.ok(this.productosService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductos(
            @PathVariable String nombreTienda,
            @PathVariable Long id) {
        this.productosService.delete(id);
        return ResponseEntity.noContent().build();
    }
}