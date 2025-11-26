package back.ecommerce.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

// 👇 IMPORTAR ESTOS 3 PARA SWAGGER
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import back.ecommerce.dtos.ProductosRequest;
import back.ecommerce.dtos.ProductosResponse;
import back.ecommerce.services.ProductosService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/tiendas/{nombreTienda}/productos")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class ProductosController {

    private final ProductosService productosService;

    @GetMapping
    public ResponseEntity<List<ProductosResponse>> getAllByTienda(
            @PathVariable String nombreTienda,
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(this.productosService.readAllByTienda(nombreTienda, sort));
    }

    // ✅ MÉTODO MODIFICADO PARA SWAGGER
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductosResponse> postProductos(
            @PathVariable String nombreTienda,
            
            // 👇 ESTA ANOTACIÓN ES LA CLAVE: Define el Content-Type explícito para Swagger
            @Parameter(description = "JSON del producto", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductosRequest.class)))
            @Valid @RequestPart("producto") ProductosRequest request, 
            
            @RequestPart(value = "file", required = false) MultipartFile file) {

        final var producto = this.productosService.create(nombreTienda, request, file);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/tienda/{nombreTienda}/productos/{id}")
                .buildAndExpand(nombreTienda, producto.getId())
                .toUri();

        return ResponseEntity.created(location).body(producto);
    }

    // ... resto de los métodos (buscar, categoria, getById, update, delete) siguen igual ...
    @GetMapping("/buscar")
    public ResponseEntity<List<ProductosResponse>> buscarProductos(
            @PathVariable String nombreTienda,
            @RequestParam("q") String termino) {
        return ResponseEntity.ok(this.productosService.buscarPorNombre(nombreTienda, termino));
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<ProductosResponse>> buscarPorCategoria(
            @PathVariable String nombreTienda,
            @PathVariable Long categoriaId) {
        return ResponseEntity.ok(this.productosService.buscarPorCategoria(nombreTienda, categoriaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductosResponse> getProductosById(
            @PathVariable String nombreTienda,
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