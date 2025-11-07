package back.ecommerce.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference; // <-- IMPORT
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany; // <-- IMPORT
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List; // <-- IMPORT

@Getter
@Setter
@Entity
@Table(name = "productos")
@AllArgsConstructor
@NoArgsConstructor
public class ProductosEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer stock;
    private String imagen;
    
    // "Hijo" de la relación con Categorias
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    @JsonBackReference("categoria-producto")
    private CategoriasEntity categoria;

    // "Padre" de la relación con ItemsPedidos
    @OneToMany(mappedBy = "producto")
    @JsonManagedReference("producto-item")
    private List<ItemsPedidosEntity> itemsPedido;
}