package back.ecommerce.controllers;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import back.ecommerce.dtos.TiendaRequest;
import back.ecommerce.dtos.TiendaResponse;
import back.ecommerce.services.TiendaService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/tiendas") // Endpoint base para gestión de tiendas
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class TiendaController {

    private final TiendaService tiendaService;

    @PostMapping
    public ResponseEntity<TiendaResponse> crearTienda(@Valid @RequestBody TiendaRequest request) {
        var tiendaCreada = tiendaService.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{nombreUrl}")
                .buildAndExpand(tiendaCreada.getNombreUrl())
                .toUri();

        return ResponseEntity.created(location).body(tiendaCreada);
    }

    @GetMapping("/{nombreUrl}")
    public ResponseEntity<TiendaResponse> obtenerTienda(@PathVariable String nombreUrl) {
        return ResponseEntity.ok(tiendaService.readByNombreUrl(nombreUrl));
    }
}