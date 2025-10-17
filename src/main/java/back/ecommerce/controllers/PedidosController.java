package back.ecommerce.controllers;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import back.ecommerce.dtos.PedidosRequest;
import back.ecommerce.dtos.PedidosResponse;
import back.ecommerce.services.PedidosService;
import lombok.AllArgsConstructor;

@RestController// use to expose RESTFULL
@RequestMapping(path = "pedidos")//wat to get this controller
@CrossOrigin(origins = "*") // Permitir solicitudes desde cualquier origen
@AllArgsConstructor
public class PedidosController {

    private final PedidosService pedidosService;

    @GetMapping(path = "{id}")//use to get data
    public ResponseEntity<PedidosResponse> getPedidos(@PathVariable Long id) {
        return ResponseEntity.ok(this.pedidosService.readById(id));
    }

    @PostMapping//use to create data
    public ResponseEntity<?> postPedidos(@RequestBody PedidosRequest request){

    final var pedido = this.pedidosService.create(request);
    
    // 1. Construir la URI completa y absoluta
    URI location = ServletUriComponentsBuilder
        .fromCurrentRequest() // Toma la URL base actual (ej: http://localhost:8080/ecommerce/pedidos)
        .path("/{id}") // Agrega el segmento /ID
        .buildAndExpand(pedido.getId()) // Sustituye {id} por el valor real
        .toUri();
        
    // 2. Devolver 201 Created con el encabezado Location correcto Y el cuerpo del pedido
    return ResponseEntity
        .created(location) // <- URI COMPLETA aquí
        .body(pedido); // <- Incluir el recurso creado en el cuerpo es útil
    }

    @PatchMapping(path = "{id}")//use to update data
    public ResponseEntity<PedidosResponse> updatePedidos(
        @PathVariable Long id, 
        @RequestBody PedidosRequest request
    ){
        return ResponseEntity.ok(this.pedidosService.update(id, request));
    }

    @DeleteMapping(path = "{id}")
    public ResponseEntity<Void> deletePedidos(@PathVariable Long id){
        this.pedidosService.delete(id);
        return ResponseEntity.noContent().build();
    }


}
