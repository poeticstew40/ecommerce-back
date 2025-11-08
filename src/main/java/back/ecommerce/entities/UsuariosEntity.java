package back.ecommerce.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Entity;
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

    @OneToMany(mappedBy = "usuario")
    @JsonManagedReference("usuario-pedido")
    private List<PedidosEntity> pedidos;
}