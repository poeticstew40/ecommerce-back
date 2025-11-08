package back.ecommerce.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference; // <-- IMPORT
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne; // <-- IMPORT
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; // <-- IMPORT

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
    
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    @JsonBackReference("categoria-producto")
    private CategoriasEntity categoria;

    @OneToMany(mappedBy = "producto")
    @JsonManagedReference("producto-item")
    private List<ItemsPedidosEntity> itemsPedido;
}