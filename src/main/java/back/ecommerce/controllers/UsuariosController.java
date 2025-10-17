package back.ecommerce.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import back.ecommerce.dtos.UsuariosResponse;
import back.ecommerce.services.UsuariosService;
import lombok.AllArgsConstructor;


@RestController// use to expose RESTFULL
@RequestMapping(path = "usuarios")//wat to get this controller
@CrossOrigin(origins = "*") // Permitir solicitudes desde cualquier origen
@AllArgsConstructor
public class UsuariosController {

    private final UsuariosService usuariosService;


    @GetMapping(path = "{dni}")//use to get data
    public ResponseEntity<UsuariosResponse> getUsuarios(@PathVariable Long dni) {
         return ResponseEntity.ok(this.usuariosService.readById(dni));
    }
    


}
