package back.ecommerce.entities;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "producto_imagenes", joinColumns = @JoinColumn(name = "producto_id"))
    @Column(name = "imagen_url")
    private List<String> imagenes = new ArrayList<>(); 

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    @JsonBackReference("categoria-producto")
    private CategoriasEntity categoria;

    @OneToMany(mappedBy = "producto")
    @JsonManagedReference("producto-item")
    private List<ItemsPedidosEntity> itemsPedido;

    @ManyToOne
    @JoinColumn(name = "tienda_id")
    @JsonBackReference("tienda-producto")
    private TiendaEntity tienda;
}