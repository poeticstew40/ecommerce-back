package back.ecommerce.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProductosRequest {

    private Long id;
    private Long categoriaId;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer stock;
    private String imagen;
}
