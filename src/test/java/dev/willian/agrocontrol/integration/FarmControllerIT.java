package dev.willian.agrocontrol.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fluxo ponta a ponta: registra usuario -> obtem token -> cria fazenda -> lista.
 * Exercita seguranca JWT, Flyway, persistencia e serializacao reais.
 */
class FarmControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullFarmLifecycle_register_createFarm_list() throws Exception {
        String registerBody = """
                {"name":"Willian","email":"willian@fazenda.dev","password":"senha123"}
                """;

        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        JsonNode registerJson = objectMapper.readTree(registerResult.getResponse().getContentAsString());
        String token = registerJson.get("token").asText();
        assertThat(token).isNotBlank();

        String farmBody = """
                {"name":"Fazenda Integracao","totalAreaHa":500.00,"city":"Toledo","state":"PR"}
                """;

        mockMvc.perform(post("/api/v1/farms")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(farmBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Fazenda Integracao"));

        mockMvc.perform(get("/api/v1/farms")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Fazenda Integracao"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void accessingProtectedEndpointWithoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/farms"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Prova o isolamento multiusuario: o usuario B nunca enxerga nem acessa dados do usuario A.
     * Este e o teste mais importante do projeto - garante que a regra de seguranca central funciona.
     */
    @Test
    void userCannotAccessAnotherUsersFarm_shouldReturn404() throws Exception {
        String tokenA = registerAndGetToken("ana@fazenda.dev");
        String tokenB = registerAndGetToken("bruno@fazenda.dev");

        // Usuario A cria uma fazenda
        String farmBody = """
                {"name":"Fazenda da Ana","totalAreaHa":300.00,"city":"Toledo","state":"PR"}
                """;
        MvcResult created = mockMvc.perform(post("/api/v1/farms")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(farmBody))
                .andExpect(status().isCreated())
                .andReturn();
        long farmId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        // Usuario B nao ve a fazenda de A na listagem
        mockMvc.perform(get("/api/v1/farms")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        // Usuario B tenta acessar a fazenda de A diretamente: 404 (sem vazar existencia)
        mockMvc.perform(get("/api/v1/farms/" + farmId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    private String registerAndGetToken(String email) throws Exception {
        String body = """
                {"name":"Teste","email":"%s","password":"senha123"}
                """.formatted(email);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }
}
