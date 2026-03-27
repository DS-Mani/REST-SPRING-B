package com.example.productapi;

import com.example.productapi.dto.PagedResponse;
import com.example.productapi.dto.ProductRequest;
import com.example.productapi.dto.ProductResponse;
import com.example.productapi.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductApiApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void contextLoads() {
    }

    // ── POST ──────────────────────────────────────

    @Test
    void createProduct_shouldReturn201() throws Exception {
        ProductRequest request = new ProductRequest(
                "Laptop", "A powerful laptop", new BigDecimal("999.99"), 10, "Electronics");

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(999.99));
    }

    @Test
    void createProduct_withInvalidData_shouldReturn400() throws Exception {
        ProductRequest request = new ProductRequest(
                "", null, new BigDecimal("-1"), -5, "");

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isMap())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    // ── GET (single) ─────────────────────────────

    @Test
    void getProduct_shouldReturn200() throws Exception {
        String body = createSampleProduct();
        ProductResponse created = objectMapper.readValue(body, ProductResponse.class);

        mockMvc.perform(get("/api/products/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    void getProduct_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("not found")));
    }

    // ── GET (list with pagination) ───────────────

    @Test
    void getAllProducts_shouldReturnPagedResponse() throws Exception {
        createSampleProduct();
        createSampleProduct();

        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sortBy", "name")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    // ── PUT ───────────────────────────────────────

    @Test
    void updateProduct_shouldReturn200() throws Exception {
        String body = createSampleProduct();
        ProductResponse created = objectMapper.readValue(body, ProductResponse.class);

        ProductRequest updateReq = new ProductRequest(
                "Updated Laptop", "Updated description", new BigDecimal("1099.99"), 5, "Electronics");

        mockMvc.perform(put("/api/products/" + created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Laptop"))
                .andExpect(jsonPath("$.price").value(1099.99));
    }

    // ── PATCH ─────────────────────────────────────

    @Test
    void patchProduct_shouldUpdateOnlyProvidedFields() throws Exception {
        String body = createSampleProduct();
        ProductResponse created = objectMapper.readValue(body, ProductResponse.class);

        String patchJson = "{\"name\": \"Patched Laptop\"}";

        mockMvc.perform(patch("/api/products/" + created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Patched Laptop"))
                .andExpect(jsonPath("$.price").value(999.99));
    }

    // ── DELETE ─────────────────────────────────────

    @Test
    void deleteProduct_shouldReturn204() throws Exception {
        String body = createSampleProduct();
        ProductResponse created = objectMapper.readValue(body, ProductResponse.class);

        mockMvc.perform(delete("/api/products/" + created.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/" + created.getId()))
                .andExpect(status().isNotFound());
    }

    // ── Search & Filter ───────────────────────────

    @Test
    void searchProducts_shouldFilterByName() throws Exception {
        createSampleProduct();

        mockMvc.perform(get("/api/products").param("search", "lap"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void filterByCategory_shouldReturnMatching() throws Exception {
        createSampleProduct();

        mockMvc.perform(get("/api/products").param("category", "Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    // ── Bad JSON & Bad Sort Field ─────────────────

    @Test
    void createProduct_withMalformedJson_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{name: broken}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON request. Please check your JSON syntax."));
    }

    @Test
    void getAllProducts_withInvalidSortField_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/products").param("sortBy", "nonexistent"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid sort field")));
    }

    // ── Helper ────────────────────────────────────

    private String createSampleProduct() throws Exception {
        ProductRequest request = new ProductRequest(
                "Laptop", "A powerful laptop", new BigDecimal("999.99"), 10, "Electronics");

        return mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }
}
