package back.ecommerce.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "items_pedidos")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ItemsPedidosEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int cantidad;
    private Double precio;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval= true)
    @JoinColumn(name = "producto_id")//FK
    private List<ProductosEntity> productos = new ArrayList<>();

    public void addProducto(ProductosEntity producto) {
        this.productos.add(producto);
    }

    public void removeProducto(ProductosEntity producto) {
        this.productos.remove(producto);
    }

    @ManyToOne
    @JoinColumn(name = "pedido_id")//FK
    private PedidosEntity pedido;



}
