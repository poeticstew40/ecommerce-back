package back.ecommerce.entities;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
    
    @Column(columnDefinition = "double default 0.0")
    private Double costoEnvio = 0.0;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tienda_banners", joinColumns = @JoinColumn(name = "tienda_id"))
    @Column(name = "banner_url")
    private List<String> banners = new ArrayList<>();

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