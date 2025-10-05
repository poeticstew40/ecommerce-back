package back.ecommerce.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PedidosRequest {

    private Long id;
    private String estado;
    private Double total;
    private List<ItemsPedidosResponse> items;
    private Long usuarioDni;

}
