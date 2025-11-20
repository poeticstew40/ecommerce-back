package back.ecommerce.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import back.ecommerce.entities.UsuariosEntity;
import back.ecommerce.repositories.CategoriasRepository;
import back.ecommerce.repositories.TiendaRepository;
import back.ecommerce.repositories.UsuariosRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class MultiTiendaFlowTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuariosRepository usuariosRepository;
    @Autowired private TiendaRepository tiendaRepository;
    @Autowired private CategoriasRepository categoriasRepository;
    @Autowired private ObjectMapper objectMapper; // Para leer los JSON de respuesta

    @Test
    @WithMockUser(username = "admin", roles = {"USER"}) // 👈 Simula estar logueado
    public void flujoCompleto_CrearTiendaYProductos_DeberiaFuncionar() throws Exception {
        
        // ==========================================
        // PASO 1: PREPARAR EL TERRENO (Base de Datos)
        // ==========================================
        
        // Guardamos un vendedor directamente en la DB para no depender del endpoint de Auth
        UsuariosEntity vendedor = new UsuariosEntity();
        vendedor.setDni(99999999L);
        vendedor.setNombre("Vendedor");
        vendedor.setApellido("Test");
        vendedor.setEmail("vendedor@test.com");
        vendedor.setPassword("123456");
        usuariosRepository.save(vendedor);

        // ==========================================
        // PASO 2: CREAR LA TIENDA (vía API)
        // ==========================================
        
        String jsonTienda = """
            {
                "nombreUrl": "tienda-test",
                "nombreFantasia": "La Tienda del Test",
                "vendedorDni": 99999999
            }
            """;

        mockMvc.perform(post("/tiendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonTienda))
                .andDo(print())
                .andExpect(status().isCreated());

        // ==========================================
        // PASO 3: CREAR CATEGORÍA EN LA TIENDA (vía API)
        // ==========================================

        String jsonCategoria = """
            { "nombre": "Gaming" }
            """;

        // Ojo: Usamos el slug "tienda-test" en la URL
        MvcResult resultCategoria = mockMvc.perform(post("/tienda/tienda-test/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonCategoria))
                .andExpect(status().isCreated())
                .andReturn();
        
        // Recuperamos el ID de la categoría creada para usarlo en el producto
        String responseCat = resultCategoria.getResponse().getContentAsString();
        // Truco rápido para sacar el ID del string JSON (o usá una clase si querés ser prolijo)
        // Asumimos que devuelve {"id": 1, ...}
        Long idCategoria = objectMapper.readTree(responseCat).get("id").asLong();

        // ==========================================
        // PASO 4: CREAR PRODUCTO EN LA TIENDA (vía API)
        // ==========================================

        String jsonProducto = """
            {
                "categoriaId": %d,
                "nombre": "Mouse Gamer",
                "descripcion": "RGB",
                "precio": 1500.0,
                "stock": 10
            }
            """.formatted(idCategoria);

        mockMvc.perform(post("/tienda/tienda-test/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonProducto))
                .andExpect(status().isCreated());

        // ==========================================
        // PASO 5: VERIFICAR QUE APARECE (GET Público)
        // ==========================================

        // Hacemos GET a la tienda correcta
        mockMvc.perform(get("/tienda/tienda-test/productos"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Mouse Gamer")) // Debería estar
                .andExpect(jsonPath("$[0].categoriaNombre").value("Gaming"));

        // ==========================================
        // PASO 6: VERIFICAR AISLAMIENTO (Tienda Incorrecta)
        // ==========================================
        
        // Creamos otra tienda "fantasma" vacía o consultamos una que no existe
        // Si consultamos "otra-tienda", la lista debería venir vacía
        mockMvc.perform(get("/tienda/otra-tienda-fake/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty()); // Debería estar vacía
    }
}