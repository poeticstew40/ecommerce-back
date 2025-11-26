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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import back.ecommerce.dtos.TiendaRequest;
import back.ecommerce.dtos.TiendaResponse;
import back.ecommerce.services.TiendaService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/tiendas")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class TiendaController {

    private final TiendaService tiendaService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TiendaResponse> crearTienda(
            @Parameter(schema = @Schema(type = "string", format = "json"))
            @RequestPart("tienda") String tiendaStr,
            @RequestPart(value = "file", required = false) MultipartFile file) throws JsonProcessingException {
        
        TiendaRequest request = objectMapper.readValue(tiendaStr, TiendaRequest.class);
        
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
            @Parameter(schema = @Schema(type = "string", format = "json"))
            @RequestPart(value = "tienda", required = false) String tiendaStr,
            @RequestPart(value = "file", required = false) MultipartFile file) throws JsonProcessingException {
        
        TiendaRequest request = new TiendaRequest();
        if (tiendaStr != null && !tiendaStr.isEmpty()) {
            request = objectMapper.readValue(tiendaStr, TiendaRequest.class);
        }
        
        return ResponseEntity.ok(tiendaService.update(nombreUrl, request, file));
    }
}