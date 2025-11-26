package back.ecommerce.controllers;

import java.net.URI;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import back.ecommerce.dtos.TiendaRequest;
import back.ecommerce.dtos.TiendaResponse;
import back.ecommerce.services.TiendaService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/tiendas")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class TiendaController {

    private final TiendaService tiendaService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TiendaResponse> crearTienda(
            @Parameter(description = "Datos de la tienda", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TiendaRequest.class)))
            @Valid @RequestPart("tienda") TiendaRequest request,
            
            @RequestPart(value = "file", required = false) MultipartFile file) {
        
        var tiendaCreada = tiendaService.create(request, file);
        
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

    @PatchMapping(value = "/{nombreUrl}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TiendaResponse> actualizarTienda(
            @PathVariable String nombreUrl,
            
            @Parameter(description = "Datos a actualizar", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TiendaRequest.class)))
            @RequestPart(value = "tienda", required = false) TiendaRequest request,
            
            @RequestPart(value = "file", required = false) MultipartFile file) {
        
        TiendaRequest safeRequest = request != null ? request : new TiendaRequest();
        
        return ResponseEntity.ok(tiendaService.update(nombreUrl, safeRequest, file));
    }
}