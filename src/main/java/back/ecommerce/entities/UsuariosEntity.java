package back.ecommerce.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "usuarios")
@AllArgsConstructor
@NoArgsConstructor
public class UsuariosEntity {

    @Id
    private Long dni;
    private String email;
    private String password;
    private String nombre;
    private String apellido;

    // ✅ NUEVO: Rol
    @Enumerated(EnumType.STRING)
    private Rol rol;

    // ✅ NUEVO: Si es vendedor, tiene una tienda asociada
    // Usamos JsonIgnore o BackReference para no hacer bucle
    // Pero por ahora dejalo simple, después lo ajustamos con JWT
    
    @OneToMany(mappedBy = "usuario")
    @JsonManagedReference("usuario-pedido")
    private List<PedidosEntity> pedidos; // Compras que hizo como COMPRADOR
}