package back.ecommerce.controllers;

// --- Imports necesarios ---
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder; // <-- Importado

// --- Tus DTOs, Service y Lombok ---
import back.ecommerce.dtos.UsuariosRequest;
import back.ecommerce.dtos.UsuariosResponse;
import back.ecommerce.services.UsuariosService;
import lombok.AllArgsConstructor;


@RestController
@RequestMapping(path = "usuarios") // <-- Corregido para seguir tu patrón
@CrossOrigin(origins = "*") 
@AllArgsConstructor
public class UsuariosController {

    private final UsuariosService usuariosService;

    // --- C: CREATE (Crear) ---
    @PostMapping
    public ResponseEntity<UsuariosResponse> crearUsuario(@RequestBody UsuariosRequest request) {
        
        // 1. Llama al servicio para crear el usuario
        final var usuarioCreado = usuariosService.create(request);

        // 2. Construye la URI de respuesta exactamente como lo hacés vos
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest() // Toma la URL base actual (ej: /ecommerce/usuarios)
            .path("/{dni}") // Agrega el segmento /DNI
            .buildAndExpand(usuarioCreado.getDni()) // Sustituye {dni} por el valor real
            .toUri();
        
        // 3. Devuelve 201 Created con la locación y el objeto creado
        return ResponseEntity
            .created(location)
            .body(usuarioCreado);
    }
    
    // --- R: READ (Leer Todos) ---
    @GetMapping()
    public ResponseEntity<List<UsuariosResponse>> obtenerTodosLosUsuarios() {
        return ResponseEntity.ok(usuariosService.readAll());
    }

    // --- R: READ (Leer Uno) ---
    @GetMapping(path = "{dni}")
    public ResponseEntity<UsuariosResponse> getUsuarios(@PathVariable Long dni) {
       return ResponseEntity.ok(this.usuariosService.readByDni(dni));
    }
    
    // --- U: UPDATE (Actualizar) ---
    @PatchMapping(path = "{dni}")
    public ResponseEntity<UsuariosResponse> actualizarUsuario(@PathVariable Long dni, @RequestBody UsuariosRequest request) {
        return ResponseEntity.ok(usuariosService.update(dni, request));
    }

    // --- D: DELETE (Borrar) ---
    @DeleteMapping(path = "{dni}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long dni) {
        usuariosService.delete(dni);
        return ResponseEntity.noContent().build();
    }
}