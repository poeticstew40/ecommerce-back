package back.ecommerce.dtos;

import java.util.List;
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
    private List<String> banners;
    private Double costoEnvio;
}