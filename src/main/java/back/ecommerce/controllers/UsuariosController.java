package back.ecommerce.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import back.ecommerce.dtos.UsuariosRequest;
import back.ecommerce.dtos.UsuariosResponse;
import back.ecommerce.services.UsuariosService;
import lombok.AllArgsConstructor;


@RestController
@RequestMapping(path = "usuarios")
@CrossOrigin(origins = "*") 
@AllArgsConstructor
public class UsuariosController {

    private final UsuariosService usuariosService;

    @GetMapping()
    public ResponseEntity<List<UsuariosResponse>> obtenerTodosLosUsuarios() {
        return ResponseEntity.ok(usuariosService.readAll());
    }

    @GetMapping(path = "{dni}")
    public ResponseEntity<UsuariosResponse> getUsuarios(@PathVariable Long dni) {
       return ResponseEntity.ok(this.usuariosService.readByDni(dni));
    }
    
    @PatchMapping(path = "{dni}")
    public ResponseEntity<UsuariosResponse> actualizarUsuario(@PathVariable Long dni, @RequestBody UsuariosRequest request) {
        return ResponseEntity.ok(usuariosService.update(dni, request));
    }

    @DeleteMapping(path = "{dni}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long dni) {
        usuariosService.delete(dni);
        return ResponseEntity.noContent().build();
    }
}