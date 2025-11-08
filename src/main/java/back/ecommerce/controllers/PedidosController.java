package back.ecommerce.controllers;

import java.net.URI;
import java.util.List;

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

@RestController
@RequestMapping(path = "pedidos")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class PedidosController {

    private final PedidosService pedidosService;

    @GetMapping(path = "{id}")
    public ResponseEntity<PedidosResponse> getPedidos(@PathVariable Long id) {
        return ResponseEntity.ok(this.pedidosService.readById(id));
    }

    @PostMapping
    public ResponseEntity<PedidosResponse> postPedidos(@RequestBody PedidosRequest request){

    final var pedido = this.pedidosService.create(request);
    
    
    URI location = ServletUriComponentsBuilder
            .fromCurrentRequest() // Toma la URL base actual
            .path("/{id}") // Agrega el segmento /ID
            .buildAndExpand(pedido.getId()) // Sustituye {id} por el valor real
            .toUri();
            
    
    return ResponseEntity
            .created(location)
            .body(pedido);
    }

    @PatchMapping(path = "{id}")
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

    @GetMapping(path = "usuario/{dni}")
    public ResponseEntity<List<PedidosResponse>> getPedidosByUsuarioDni(@PathVariable Long dni) {
        final List<PedidosResponse> pedidos = this.pedidosService.findByUsuarioDni(dni);
        return ResponseEntity.ok(pedidos);
    }
}