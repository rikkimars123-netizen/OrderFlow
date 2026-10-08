package org.example.orderflow.controller;

import org.example.orderflow.config.TestCacheConfig;
import org.example.orderflow.security.CustomUserDetailsService;
import org.example.orderflow.security.JwtService;
import org.example.orderflow.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestCacheConfig.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "anton", roles = "USER")
    void shouldCreatePayment() throws Exception {

        mockMvc.perform(post("/payments/1"))
                .andExpect(status().isCreated());

        verify(paymentService)
                .createPayment(1, "anton");
    }

    @Test
    @WithMockUser(username = "anton", roles = "USER")
    void shouldSuccessPayment() throws Exception {

        mockMvc.perform(patch("/payments/1/success"))
                .andExpect(status().isOk());

        verify(paymentService)
                .successPayment(1, "anton");
    }

    @Test
    @WithMockUser(username = "anton", roles = "USER")
    void shouldFailPayment() throws Exception {

        mockMvc.perform(patch("/payments/1/failed"))
                .andExpect(status().isOk());

        verify(paymentService)
                .failedPayment(1, "anton");
    }
}