package com.fraudshield.alert.controller;


import com.fraudshield.alert.context.UserContext;
import com.fraudshield.alert.model.Alert;
import com.fraudshield.alert.model.AlertStatus;
import com.fraudshield.alert.repository.AlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Alert Controller Tests")
public class AlertControllerTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private UserContext userContext;

    @InjectMocks
    private AlertController alertController;

    private MockMvc mockMvc;
    private Alert alert;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(alertController)
                .build();

        alert = Alert.builder()
                .transactionId("txn-001")
                .userId("user-001")
                .merchantId("merchant-001")
                .amount(new BigDecimal("75000"))
                .fraudStatus("BLOCKED")
                .alertStatus(AlertStatus.PENDING)
                .riskScore(95.0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should get my alerts for USER role")
    void shouldGetMyAlertsForUserRole() throws Exception {
        when(userContext.getCurrentUserId())
                .thenReturn("user-001");
        when(userContext.getCurrentUserRole())
                .thenReturn("USER");
        when(alertRepository.findByUserId("user-001"))
                .thenReturn(List.of(alert));

        mockMvc.perform(get("/api/alerts/my-alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId")
                        .value("user-001"));
    }

    @Test
    @DisplayName("Should get all alerts for ADMIN role")
    void shouldGetAllAlertsForAdminRole() throws Exception {
        when(userContext.getCurrentUserId())
                .thenReturn("admin-001");
        when(userContext.getCurrentUserRole())
                .thenReturn("ADMIN");
        when(alertRepository.findAll())
                .thenReturn(List.of(alert));

        mockMvc.perform(get("/api/alerts/my-alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should get alerts by user ID")
    void shouldGetAlertsByUserId() throws Exception {
        when(alertRepository.findByUserId("user-001"))
                .thenReturn(List.of(alert));

        mockMvc.perform(get("/api/alerts/user/user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId")
                        .value("user-001"));
    }

    @Test
    @DisplayName("Should get alerts by status")
    void shouldGetAlertsByStatus() throws Exception {
        when(alertRepository.findByFraudStatus("BLOCKED"))
                .thenReturn(List.of(alert));

        mockMvc.perform(get("/api/alerts/status/BLOCKED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].fraudStatus")
                        .value("BLOCKED"));
    }

}
