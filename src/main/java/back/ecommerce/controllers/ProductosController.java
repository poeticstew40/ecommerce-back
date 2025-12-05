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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import back.ecommerce.dtos.ProductosRequest;
import back.ecommerce.dtos.ProductosResponse;
import back.ecommerce.services.ProductosService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/tiendas/{nombreTienda}/productos")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class ProductosController {

    private final ProductosService productosService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<List<ProductosResponse>> getAllByTienda(
            @PathVariable String nombreTienda,
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(this.productosService.readAllByTienda(nombreTienda, sort));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductosResponse> postProductos(
            @PathVariable String nombreTienda,
            @Parameter(schema = @Schema(type = "string", format = "json"))
            @RequestPart("producto") String productoStr, 
            @RequestPart(value = "files", required = false) List<MultipartFile> files) throws JsonProcessingException {

        ProductosRequest request = objectMapper.readValue(productoStr, ProductosRequest.class);
        final var producto = this.productosService.create(nombreTienda, request, files);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/tienda/{nombreTienda}/productos/{id}")
                .buildAndExpand(nombreTienda, producto.getId())
                .toUri();
        return ResponseEntity.created(location).body(producto);
    }

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

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductosResponse> updateProductos(
            @PathVariable String nombreTienda,
            @PathVariable Long id,
            @Parameter(schema = @Schema(type = "string", format = "json"))
            @RequestPart(value = "producto", required = false) String productoStr,
            @RequestPart(value = "files", required = false) List<MultipartFile> 
            files) throws JsonProcessingException {
        
        ProductosRequest request = new ProductosRequest();
        if (productoStr != null) {
            request = objectMapper.readValue(productoStr, ProductosRequest.class);
        }
        
        return ResponseEntity.ok(this.productosService.update(id, request, files));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductos(
            @PathVariable String nombreTienda,
            @PathVariable Long id) {
        this.productosService.delete(id);
        return ResponseEntity.noContent().build();
    }
}