package back.ecommerce.controllers;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import back.ecommerce.dtos.PedidosRequest;
import back.ecommerce.dtos.PedidosResponse;
import back.ecommerce.services.PedidosService;
import lombok.AllArgsConstructor;

@RestController// use to expose RESTFULL
@RequestMapping(path = "pedidos")//wat to get this controller
@AllArgsConstructor
public class PedidosController {

    private final PedidosService pedidosService;

    @GetMapping(path = "{id}")//use to get data
    public ResponseEntity<PedidosResponse> getPedidos(@PathVariable Long id) {
        return ResponseEntity.ok(this.pedidosService.readById(id));
    }

    @PostMapping//use to create data
    public ResponseEntity<PedidosResponse> postPedidos(@RequestBody PedidosRequest request){

            // 1. Ejecutar el servicio para crear el pedido.
    final var createdResponse = this.pedidosService.create(request);
    
    // 2. CORRECCIÓN: Usar el ID del pedido (createdResponse.getId()) para la URI
    //    Esto asume que tu DTO PedidosResponse tiene un campo getId() con el ID.
    //    El ID es el identificador único del recurso creado.
    final var uri = URI.create("/pedidos/" + createdResponse.getId());

    // 3. Devolver el código 201 Created con la URI en el encabezado Location y el cuerpo de la respuesta.
    return ResponseEntity.created(uri).body(createdResponse);
    }

    @PutMapping//use to update data
    public ResponseEntity<?> updatePedidos(){
        return null;
    }

    @DeleteMapping
    public ResponseEntity<Void> deletePedidos(){
        return null;
    }


}
