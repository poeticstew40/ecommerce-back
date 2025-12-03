package back.ecommerce.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import back.ecommerce.entities.UsuariosEntity;
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
            @RequestPart(value = "file", required = false) MultipartFile file, 
            @RequestPart(value = "banners", required = false) List<MultipartFile> banners) throws JsonProcessingException {
        
        TiendaRequest request = objectMapper.readValue(tiendaStr, TiendaRequest.class);
        var tiendaCreada = tiendaService.create(request, file, banners);
        
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

    @GetMapping("/vendedor/{dni}")
    public ResponseEntity<TiendaResponse> getTiendaByVendedorDni(@PathVariable Long dni) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof String) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UsuariosEntity usuarioLogueado = (UsuariosEntity) principal;
        if (!usuarioLogueado.getDni().equals(dni)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            return ResponseEntity.ok(tiendaService.readByVendedorDni(dni));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping(value = "/{nombreUrl}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TiendaResponse> actualizarTienda(
            @PathVariable String nombreUrl,
            @Parameter(schema = @Schema(type = "string", format = "json"))
            @RequestPart(value = "tienda", required = false) String tiendaStr,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "banners", required = false) List<MultipartFile> banners) throws JsonProcessingException {
       
        TiendaRequest request = new TiendaRequest();
        if (tiendaStr != null && !tiendaStr.isEmpty()) {
            request = objectMapper.readValue(tiendaStr, TiendaRequest.class);
        }
        
        return ResponseEntity.ok(tiendaService.update(nombreUrl, request, file, banners));
    }
  
    @DeleteMapping("/{nombreUrl}")
    public ResponseEntity<Void> eliminarTienda(@PathVariable String nombreUrl) {
        tiendaService.delete(nombreUrl);
        return ResponseEntity.noContent().build();
    }
}