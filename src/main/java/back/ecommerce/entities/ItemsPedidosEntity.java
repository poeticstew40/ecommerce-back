package back.ecommerce.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "items_pedidos")
@AllArgsConstructor
@NoArgsConstructor
public class ItemsPedidosEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int cantidad;
    private Double precioUnitario;

    @ManyToOne
    @JoinColumn(name = "pedido_id")
    @JsonBackReference("pedido-item")
    private PedidosEntity pedido;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    @JsonBackReference("producto-item")
    private ProductosEntity producto;
}