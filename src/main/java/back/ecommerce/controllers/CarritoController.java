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

import back.ecommerce.dtos.CarritoRequest;
import back.ecommerce.dtos.CarritoResponse;
import back.ecommerce.services.CarritoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tiendas/{nombreTienda}/carrito")
@CrossOrigin(origins = "*") 
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;

    @PostMapping("/agregar")
    public ResponseEntity<CarritoResponse> agregarAlCarrito(
            @PathVariable String nombreTienda,
            @Valid @RequestBody CarritoRequest request) {
        
        return ResponseEntity.ok(carritoService.agregarProducto(nombreTienda, request));
    }

    @GetMapping("/{usuarioDni}")
    public ResponseEntity<List<CarritoResponse>> verCarrito(
            @PathVariable String nombreTienda, 
            @PathVariable Long usuarioDni) {
        return ResponseEntity.ok(carritoService.obtenerCarrito(usuarioDni));
    }

    @DeleteMapping("/item/{idItem}")
    public ResponseEntity<Void> eliminarItem(
            @PathVariable String nombreTienda, 
            @PathVariable Long idItem) {
        carritoService.eliminarItem(idItem);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/vaciar/{usuarioDni}")
    public ResponseEntity<Void> vaciarCarrito(
            @PathVariable String nombreTienda, 
            @PathVariable Long usuarioDni) {
        carritoService.vaciarCarrito(usuarioDni);
        return ResponseEntity.noContent().build();
    }
}