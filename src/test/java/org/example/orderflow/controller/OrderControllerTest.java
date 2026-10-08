package org.example.orderflow.controller;

import org.example.orderflow.config.TestCacheConfig;
import org.example.orderflow.dto.CreateOrderRequest;
import org.example.orderflow.dto.OrderItemRequest;
import org.example.orderflow.security.CustomUserDetailsService;
import org.example.orderflow.security.JwtService;
import org.example.orderflow.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestCacheConfig.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;
    @Test
    @WithMockUser(username = "anton", roles = "USER")

    void shouldCreateOrder() throws Exception {

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1);
        item.setCount(2);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setItems(List.of(item));

        mockMvc.perform(
                        post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());

        verify(orderService)
                .createOrder(eq("anton"), any(CreateOrderRequest.class));
    }

    @Test
    @WithMockUser(username = "anton", roles = "USER")
    void shouldCancelOrder() throws Exception {

        mockMvc.perform(
                        patch("/orders/5/cancel")
                )
                .andExpect(status().isOk());

        verify(orderService)
                .cancelOrder(5, "anton");
    }

    @Test
    @WithMockUser(username = "anton", roles = "USER")
    void shouldReturnBadRequestWhenItemCountIsInvalid() throws Exception {

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1);
        item.setCount(0);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setItems(List.of(item));

        mockMvc.perform(
                        post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(orderService, never())
                .createOrder(any(), any());
    }
}