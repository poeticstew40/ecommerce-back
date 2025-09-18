package back.ecommerce.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ItemsPedidosResponse {

    private int cantidad;
    private Double precioUnitario;
    private String nombreProducto;
    private String descripcionProducto;

}
