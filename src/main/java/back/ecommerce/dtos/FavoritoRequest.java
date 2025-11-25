package back.ecommerce.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FavoritoRequest {
    @NotNull private Long usuarioDni;
    @NotNull private Long productoId;
}