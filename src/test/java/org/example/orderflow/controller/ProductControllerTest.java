package org.example.orderflow.controller;

import org.example.orderflow.config.TestCacheConfig;
import org.example.orderflow.dto.CreateProduct;
import org.example.orderflow.security.CustomUserDetailsService;
import org.example.orderflow.security.JwtService;
import org.example.orderflow.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestCacheConfig.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldCreateProduct() throws Exception {

        CreateProduct request = new CreateProduct(
                "iPhone",
                new BigDecimal("1000.00"),
                1
        );

        mockMvc.perform(
                        post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());

        verify(productService)
                .createProduct(any(CreateProduct.class));
    }

    @Test
    void shouldReturnBadRequestWhenNameIsBlank() throws Exception {

        CreateProduct request = new CreateProduct(
                " ",
                new BigDecimal("1000.00"),
                1
        );

        mockMvc.perform(
                        post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(productService, never())
                .createProduct(any(CreateProduct.class));
    }

    @Test
    void shouldReturnBadRequestWhenPriceIsNegative() throws Exception {

        CreateProduct request = new CreateProduct(
                "iPhone",
                new BigDecimal("-1"),
                1
        );

        mockMvc.perform(
                        post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(productService, never())
                .createProduct(any(CreateProduct.class));
    }
}