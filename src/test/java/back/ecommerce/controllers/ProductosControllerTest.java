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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional 
@AutoConfigureTestDatabase(connection= EmbeddedDatabaseConnection.H2)
public class ProductosControllerTest {

    @Autowired
    private MockMvc mockMvc; // El robot que hace las peticiones

    // CASO 1: Intentar crear un producto VÁLIDO (Debería dar 201)
    @Test
    public void crearProductoValido_DeberiaRetornarCreated() throws Exception {
        // JSON válido (con todos los datos)
        String jsonRequest = """
            {
                "categoriaId": 1,
                "nombre": "Teclado Gamer Test",
                "descripcion": "Test automático",
                "precio": 5000.0,
                "stock": 10,
                "imagen": "img.jpg"
            }
            """;

        mockMvc.perform(post("/productos") // Hacé un POST a /productos
                .contentType(MediaType.APPLICATION_JSON) // Avisá que mandás JSON
                .content(jsonRequest)) // Poné el cuerpo
                .andExpect(status().isCreated()) // ✅ ESPERO: Status 201 Created
                .andExpect(jsonPath("$.nombre").value("Teclado Gamer Test")); // Y que el nombre sea el correcto
    }

    // CASO 2: Intentar crear un producto INVÁLIDO (Debería dar 400 Bad Request)
    // Esto prueba que tu @Valid y tu ErrorHandlerController funcionan.
    @Test
    public void crearProductoSinNombre_DeberiaRetornarBadRequest() throws Exception {
        // JSON inválido (falta el nombre y el precio es negativo)
        String jsonRequest = """
            {
                "categoriaId": 1,
                "precio": -100.0, 
                "stock": 10
            }
            """;

        mockMvc.perform(post("/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest()) // ✅ ESPERO: Status 400 Bad Request
                .andExpect(jsonPath("$.errors.nombre").exists()) // Espero que el error mencione el campo "nombre"
                .andExpect(jsonPath("$.errors.precio").exists()); // Espero que el error mencione el campo "precio"
    }
}
