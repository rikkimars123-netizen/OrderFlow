package org.example.orderflow.controller;

import org.example.orderflow.config.TestCacheConfig;
import org.example.orderflow.security.CustomUserDetailsService;
import org.example.orderflow.security.JwtService;
import org.example.orderflow.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.example.orderflow.config.SecurityConfig;
import org.springframework.context.annotation.Import;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import({SecurityConfig.class, TestCacheConfig.class})
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldDenyUnauthenticatedUser() throws Exception {

        mockMvc.perform(get("/products"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "anton", roles = "USER")
    void userShouldAccessProducts() throws Exception {

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "anton", roles = "USER")
    void userShouldNotCreateProduct() throws Exception {

        mockMvc.perform(
                        post("/products")
                                .contentType("application/json")
                                .content("""
                                    {
                                      "name": "iPhone",
                                      "price": 1000,
                                      "categoryId": 1
                                    }
                                    """)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminShouldReachCreateProduct() throws Exception {

        mockMvc.perform(
                        post("/products")
                                .contentType("application/json")
                                .content("""
                                    {
                                      "name": "iPhone",
                                      "price": 1000,
                                      "categoryId": 1
                                    }
                                    """)
                )
                .andExpect(status().isCreated());
    }
}