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

    private String estado;
    private Double total;
    private LocalDateTime fechaPedido;
    private List<ItemsPedidosResponse> items;

}
