package back.ecommerce.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import back.ecommerce.entities.CategoriasEntity;
import back.ecommerce.entities.ProductosEntity;
import back.ecommerce.entities.UsuariosEntity;
import back.ecommerce.repositories.CategoriasRepository;
import back.ecommerce.repositories.ProductosRepository;
import back.ecommerce.repositories.UsuariosRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@AutoConfigureTestDatabase(connection= EmbeddedDatabaseConnection.H2)
public class PedidosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuariosRepository usuariosRepository;
    @Autowired
    private ProductosRepository productosRepository;
    @Autowired
    private CategoriasRepository categoriasRepository;

    @Test
    public void crearPedido_DeberiaCalcularTotalAutomaticamente() throws Exception {
        // 1. PREPARAR DATOS
        
        UsuariosEntity usuario = new UsuariosEntity();
        usuario.setDni(123123L);
        usuario.setEmail("comprador@test.com");
        usuario.setNombre("Comprador");
        usuario.setApellido("Test");
        usuario.setPassword("123456");
        usuariosRepository.save(usuario);

        CategoriasEntity categoria = new CategoriasEntity();
        categoria.setNombre("General");
        categoriasRepository.save(categoria);

        ProductosEntity producto = new ProductosEntity();
        producto.setNombre("Producto Test");
        producto.setPrecio(100.0); // PRECIO = 100
        producto.setStock(10);
        producto.setCategoria(categoria);
        ProductosEntity productoGuardado = productosRepository.save(producto);

        // 2. JSON REQUEST
        // Pedimos 2 unidades (Total debería ser 200.0)
        String jsonRequest = """
            {
                "usuarioDni": 123123,
                "items": [
                    {
                        "idProducto": %d,
                        "cantidad": 2
                    }
                ]
            }
            """.formatted(productoGuardado.getId()); 

        // 3. EJECUTAR
        mockMvc.perform(post("/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andDo(print()) // 👈 MIRA LA CONSOLA ACÁ
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(200.0)) // Verifica el cálculo
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }
}