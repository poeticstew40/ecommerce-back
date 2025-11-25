package back.ecommerce.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FavoritoResponse {
    private Long id;
    private Long productoId;
    private String nombreProducto;
    private String imagen;
    private Double precio;
}