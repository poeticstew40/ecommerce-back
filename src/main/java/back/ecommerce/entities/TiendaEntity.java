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
    private String nombreUrl; // El "slug" (ej: "tienda-de-juan")

    private String nombreFantasia; // El nombre lindo (ej: "La Tienda de Juan")
    private String logo; // URL de la imagen
    private String descripcion;

    // Relación con el DUEÑO (Vendedor)
    @OneToOne
    @JoinColumn(name = "vendedor_dni")
    private UsuariosEntity vendedor;

    // Relación con sus PRODUCTOS
    @OneToMany(mappedBy = "tienda", cascade = CascadeType.ALL)
    @JsonManagedReference("tienda-producto")
    private List<ProductosEntity> productos;

    // Relación con sus CATEGORÍAS
    @OneToMany(mappedBy = "tienda", cascade = CascadeType.ALL)
    @JsonManagedReference("tienda-categoria")
    private List<CategoriasEntity> categorias;
    
    // Relación con sus PEDIDOS
    @OneToMany(mappedBy = "tienda")
    @JsonManagedReference("tienda-pedido")
    private List<PedidosEntity> pedidos;
}