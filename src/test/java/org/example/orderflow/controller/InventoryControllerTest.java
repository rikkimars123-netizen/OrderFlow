package org.example.orderflow.controller;

import org.example.orderflow.config.TestCacheConfig;
import org.example.orderflow.security.CustomUserDetailsService;
import org.example.orderflow.security.JwtService;
import org.example.orderflow.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestCacheConfig.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryService inventoryService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldCreateInventory() throws Exception {

        mockMvc.perform(
                        post("/inventory/1")
                                .param("count", "10")
                )
                .andExpect(status().isCreated());

        verify(inventoryService).createInventory(1, 10);
    }

    @Test
    void shouldAddStock() throws Exception {

        mockMvc.perform(
                        patch("/inventory/1/add")
                                .param("count", "5")
                )
                .andExpect(status().isOk());

        verify(inventoryService).addStock(1, 5);
    }

    @Test
    void shouldRemoveStock() throws Exception {

        mockMvc.perform(
                        patch("/inventory/1/remove")
                                .param("count", "3")
                )
                .andExpect(status().isOk());

        verify(inventoryService).removeStock(1, 3);
    }

    @Test
    void shouldReturnBadRequestWhenCountIsNegative() throws Exception {

        mockMvc.perform(
                        patch("/inventory/1/add")
                                .param("count", "-1")
                )
                .andExpect(status().isBadRequest());

        verify(inventoryService, never()).addStock(1, -1);
    }
}