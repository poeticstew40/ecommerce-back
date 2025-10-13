package back.ecommerce.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import back.ecommerce.dtos.ProductosResponse;
import back.ecommerce.services.ProductosService;
import lombok.AllArgsConstructor;

@RestController// use to expose RESTFULL
@RequestMapping(path = "productos")//wat to get this controller
@AllArgsConstructor
public class ProductosController {

    private final ProductosService productosService;

    //@GetMapping(path = "{nombre}")//use to get data
    //public ResponseEntity<ProductosResponse> getProductosByName(@PathVariable String nombre) {
    //     return ResponseEntity.ok(this.productosService.readByName(nombre));
    //}
    @GetMapping(path = "{id}")//use to get data
    public ResponseEntity<ProductosResponse> getProductosById (@PathVariable Long id) {
         return ResponseEntity.ok(this.productosService.readById(id));
    }
}
