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

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@AutoConfigureTestDatabase(connection= EmbeddedDatabaseConnection.H2)
public class UsuariosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void crearUsuarioValido_DeberiaRetornarCreated() throws Exception {
        String jsonRequest = """
            {
                "dni": 11223344,
                "email": "testuser@mail.com",
                "password": "password123",
                "nombre": "Test",
                "apellido": "User"
            }
            """;

        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("testuser@mail.com"));
    }

    @Test
    public void crearUsuarioEmailInvalido_DeberiaFallar() throws Exception {
        // Email inválido para forzar el error
        String jsonRequest = """
            {
                "dni": 99887766,
                "email": "esto-no-es-un-email",
                "password": "123",
                "nombre": "Fail",
                "apellido": "Man"
            }
            """;

        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andDo(print()) // 👈 ¡ACÁ VAS A VER EL ERROR EN CONSOLA!
                .andExpect(status().isBadRequest()); // Esperamos 400
    }
}