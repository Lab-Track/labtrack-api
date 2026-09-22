package com.labtrack.labtrack.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void openApiDocsDeclareBearerSchemeAndRequireItOnProtectedRoutes() throws Exception {
        JsonNode root = fetchOpenApiDocs();

        JsonNode bearerAuth = root.at("/components/securitySchemes/bearerAuth");
        assertThat(bearerAuth.get("type").asText()).isEqualTo("http");
        assertThat(bearerAuth.get("scheme").asText()).isEqualTo("bearer");

        // springdoc só grava "security" numa operação quando ela sobrescreve o padrão global;
        // rotas protegidas (como esta) herdam o requisito de security no nível raiz do documento.
        JsonNode globalSecurity = root.at("/security");
        assertThat(globalSecurity.isArray()).isTrue();
        assertThat(globalSecurity).isNotEmpty();
        assertThat(globalSecurity.get(0).has("bearerAuth")).isTrue();

        JsonNode statusEndpointSecurity = root.at("/paths/~1api~1equipment~1{id}~1status/patch/security");
        assertThat(statusEndpointSecurity.isMissingNode()).isTrue();
    }

    @Test
    void openApiDocsDocument401OnLoginAndOptOutOfBearerRequirement() throws Exception {
        JsonNode root = fetchOpenApiDocs();

        JsonNode loginResponses = root.at("/paths/~1api~1auth~1login/post/responses");
        assertThat(loginResponses.has("401")).isTrue();

        JsonNode loginSecurity = root.at("/paths/~1api~1auth~1login/post/security");
        assertThat(loginSecurity.isArray()).isTrue();
        assertThat(loginSecurity).isEmpty();
    }

    @Test
    void openApiDocsDescribeDanificadoAsBlockingLoan() throws Exception {
        JsonNode root = fetchOpenApiDocs();

        String statusEndpointDescription = root
                .at("/paths/~1api~1equipment~1{id}~1status/patch/description")
                .asText();
        assertThat(statusEndpointDescription).contains("DANIFICADO");
        assertThat(statusEndpointDescription).contains("emprestado");
    }

    private JsonNode fetchOpenApiDocs() throws Exception {
        String body = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return new ObjectMapper().readTree(body);
    }
}
