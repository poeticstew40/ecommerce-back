package back.ecommerce.entities;

import java.time.LocalDateTime;
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
@Table(name = "pedidos")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PedidosEntity {

@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaPedido;
    private String estado;
    private Double total;

    @ManyToOne
    @JoinColumn(name = "usuario_dni")
    private UsuariosEntity usuario;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemsPedidosEntity> itemsPedido;

    public void addItemPedido(ItemsPedidosEntity item) {
    if (this.itemsPedido == null) {
        this.itemsPedido = new ArrayList<>();
    }
    this.itemsPedido.add(item);
    item.setPedido(this);
    }

    public void removeItemPedido(ItemsPedidosEntity item) {
        if (this.itemsPedido != null) {
            this.itemsPedido.remove(item);
            item.setPedido(null);
        }
    }   


}