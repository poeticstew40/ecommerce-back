package back.ecommerce.dtos;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PedidosResponse {
    private Long id;
    private String estado;
    private Double total;
    private LocalDateTime fechaPedido;
    private List<ItemsPedidosResponse> items;
    
    // Datos del Comprador
    private Long usuarioDni;
    private String usuarioNombre;
    private String usuarioApellido;
    
    // Datos de Envío
    private String direccionEnvio;
    private String metodoEnvio;
    private Double costoEnvio;
}