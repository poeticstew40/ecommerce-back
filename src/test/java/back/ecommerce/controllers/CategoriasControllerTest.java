package back.ecommerce.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath; // 👈 Para ver el log
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@AutoConfigureTestDatabase(connection= EmbeddedDatabaseConnection.H2)
public class CategoriasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void crearCategoria_DeberiaRetornarCreated() throws Exception {
        String jsonRequest = """
            {
                "nombre": "Periféricos Gamer"
            }
            """;

        mockMvc.perform(post("/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andDo(print()) // 👈 Muestra el JSON de respuesta en la consola
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Periféricos Gamer"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    public void obtenerCategorias_DeberiaRetornarLista() throws Exception {
        // Primero creamos una para asegurarnos de que haya algo (por si la DB estaba vacía)
        String jsonRequest = """
            { "nombre": "Test Categoria" }
            """;
        mockMvc.perform(post("/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest));

        // Ahora probamos el GET
        mockMvc.perform(get("/categorias"))
                .andDo(print()) // 👈 Muestra la lista en la consola
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}