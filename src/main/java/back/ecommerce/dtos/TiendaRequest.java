package back.ecommerce.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TiendaRequest {

    @NotBlank(message = "El nombre URL (slug) es obligatorio")
    private String nombreUrl; // Ej: "la-ferreteria" (sin espacios)

    @NotBlank(message = "El nombre de fantasía es obligatorio")
    private String nombreFantasia; // Ej: "La Ferretería de Juan"

    private String logo;
    private String descripcion;

    @NotNull(message = "El DNI del vendedor es obligatorio")
    private Long vendedorDni;
}