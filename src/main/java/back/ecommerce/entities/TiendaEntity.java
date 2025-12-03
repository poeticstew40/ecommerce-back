package back.ecommerce.entities;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tiendas")
@AllArgsConstructor
@NoArgsConstructor
public class TiendaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nombreUrl;

    private String nombreFantasia;
    private String logo;
    private String descripcion;

    @OneToOne
    @JoinColumn(name = "vendedor_dni")
    private UsuariosEntity vendedor;

    @OneToMany(mappedBy = "tienda", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("tienda-producto")
    private List<ProductosEntity> productos;

    @OneToMany(mappedBy = "tienda", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("tienda-categoria")
    private List<CategoriasEntity> categorias;

    @OneToMany(mappedBy = "tienda", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("tienda-pedido")
    private List<PedidosEntity> pedidos;
}