package back.ecommerce.dtos;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TiendaRequest {

    @NotBlank(message = "El nombre URL es obligatorio")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "El nombre URL solo puede tener letras minúsculas, números y guiones")
    private String nombreUrl;

    @NotBlank(message = "El nombre de fantasía es obligatorio")
    private String nombreFantasia;

    private String descripcion;

    @NotNull(message = "El DNI del vendedor es obligatorio")
    private Long vendedorDni;

    private List<String> banners;
}