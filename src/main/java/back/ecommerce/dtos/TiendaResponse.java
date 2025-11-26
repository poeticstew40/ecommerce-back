package back.ecommerce.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TiendaResponse {
    private Long id;
    private String nombreUrl;
    private String nombreFantasia;
    private String logo;
    private String descripcion;
    private Long vendedorDni;
    private String vendedorNombre;
}