package dev.willian.agrocontrol.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cobre os fluxos de autenticacao e os casos de borda do JWT:
 * registro duplicado, login invalido, token ausente/invalido e o endpoint /me.
 */
class AuthControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_thenMe_shouldReturnAuthenticatedUser() throws Exception {
        String token = register("carla@fazenda.dev");

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("carla@fazenda.dev"))
                .andExpect(jsonPath("$.role").value("OPERATOR"));
    }

    @Test
    void register_withDuplicateEmail_shouldReturn409() throws Exception {
        register("dup@fazenda.dev");

        String body = """
                {"name":"Outro","email":"dup@fazenda.dev","password":"senha123"}
                """;
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void login_withWrongPassword_shouldReturn401() throws Exception {
        register("eva@fazenda.dev");

        String body = """
                {"email":"eva@fazenda.dev","password":"senhaErrada"}
                """;
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_withInvalidEmail_shouldReturn400() throws Exception {
        String body = """
                {"name":"Sem Email","email":"nao-e-email","password":"senha123"}
                """;
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void me_withMalformedToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer token.invalido.aqui"))
                .andExpect(status().isUnauthorized());
    }

    private String register(String email) throws Exception {
        String body = """
                {"name":"Teste","email":"%s","password":"senha123"}
                """.formatted(email);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }
}
