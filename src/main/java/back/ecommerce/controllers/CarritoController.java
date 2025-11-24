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
@RequestMapping("/api/carrito")
@CrossOrigin(origins = "*") // Ajustar si ya usas CorsConfig global
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;

    // Agregar producto al carrito
    @PostMapping("/agregar")
    public ResponseEntity<CarritoResponse> agregarAlCarrito(@Valid @RequestBody CarritoRequest request) {
        return ResponseEntity.ok(carritoService.agregarProducto(request));
    }

    // Ver carrito de un usuario
    @GetMapping("/{usuarioDni}")
    public ResponseEntity<List<CarritoResponse>> verCarrito(@PathVariable Long usuarioDni) {
        return ResponseEntity.ok(carritoService.obtenerCarrito(usuarioDni));
    }

    // Eliminar un item específico del carrito
    @DeleteMapping("/item/{idItem}")
    public ResponseEntity<Void> eliminarItem(@PathVariable Long idItem) {
        carritoService.eliminarItem(idItem);
        return ResponseEntity.noContent().build();
    }

    // Vaciar todo el carrito del usuario
    @DeleteMapping("/vaciar/{usuarioDni}")
    public ResponseEntity<Void> vaciarCarrito(@PathVariable Long usuarioDni) {
        carritoService.vaciarCarrito(usuarioDni);
        return ResponseEntity.noContent().build();
    }
}