package back.ecommerce.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CarritoResponse {
    private Long idItem;
    private Long productoId;
    private String nombreProducto;
    private String imagenProducto;
    private Double precioUnitario;
    private Integer cantidad;
    private Double subtotal;
    private Long tiendaId; 
    private String nombreTienda;
}