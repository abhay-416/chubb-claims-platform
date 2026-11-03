package com.chubb.claims.api;

import com.chubb.claims.api.dto.ClaimResponse;
import com.chubb.claims.api.dto.CreateClaimRequest;
import com.chubb.claims.api.dto.DashboardMetricsResponse;
import com.chubb.claims.api.dto.LiabilityExposureResponse;
import com.chubb.claims.domain.ClaimStatus;
import com.chubb.claims.domain.ClaimType;
import com.chubb.claims.domain.Market;
import com.chubb.claims.security.JwtAuthenticationFilter;
import com.chubb.claims.security.JwtTokenProvider;
import com.chubb.claims.service.AuthService;
import com.chubb.claims.service.ClaimService;
import com.chubb.claims.service.DashboardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ClaimController.class, DashboardController.class})
@AutoConfigureMockMvc
@DisplayName("Claim & Dashboard Controller MockMvc Integration Tests")
class ClaimControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClaimService claimService;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @WithMockUser(username = "claimant@gmail.com", roles = {"CLAIMANT"})
    @DisplayName("POST /api/v1/claims should return 201 Created when valid payload provided")
    void shouldCreateClaimSuccessfully() throws Exception {
        CreateClaimRequest request = new CreateClaimRequest(
            "POL-SG-99", ClaimType.MOTOR, Market.SINGAPORE, LocalDate.now(),
            "Collision on PIE expressway", new BigDecimal("4500.00")
        );

        ClaimResponse response = new ClaimResponse(
            1L, "CLM-SG-2026-1001", "POL-SG-99", ClaimType.MOTOR, Market.SINGAPORE,
            ClaimStatus.SUBMITTED, LocalDate.now(), "Collision on PIE expressway",
            new BigDecimal("4500.00"), null, 1L, "Tan Wei Ling", null, null,
            LocalDateTime.now(), LocalDateTime.now()
        );

        when(claimService.submitClaim(any(CreateClaimRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/claims")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.claimNumber").value("CLM-SG-2026-1001"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.market").value("SINGAPORE"));
    }

    @Test
    @WithMockUser(username = "claimant@gmail.com", roles = {"CLAIMANT"})
    @DisplayName("POST /api/v1/claims should return 400 Bad Request when validation fails")
    void shouldReturnBadRequestWhenPayloadInvalid() throws Exception {
        // Missing policy number and negative amount
        CreateClaimRequest invalidRequest = new CreateClaimRequest(
            "", ClaimType.MOTOR, Market.SINGAPORE, LocalDate.now(),
            "", new BigDecimal("-100.00")
        );

        mockMvc.perform(post("/api/v1/claims")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    @WithMockUser(username = "officer@chubb.com", roles = {"CLAIMS_OFFICER"})
    @DisplayName("GET /api/v1/claims should list claims")
    void shouldListClaims() throws Exception {
        ClaimResponse response = new ClaimResponse(
            1L, "CLM-SG-2026-1001", "POL-SG-99", ClaimType.MOTOR, Market.SINGAPORE,
            ClaimStatus.SUBMITTED, LocalDate.now(), "Collision",
            new BigDecimal("4500.00"), null, 1L, "Tan Wei Ling", null, null,
            LocalDateTime.now(), LocalDateTime.now()
        );

        when(claimService.listClaims(any(), any(), any(), any())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/claims"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].claimNumber").value("CLM-SG-2026-1001"));
    }

    @Test
    @WithMockUser(username = "manager@chubb.com", roles = {"CLAIMS_MANAGER"})
    @DisplayName("GET /api/v1/dashboard/liability should return liability exposure")
    void shouldReturnLiabilityExposure() throws Exception {
        LiabilityExposureResponse response = new LiabilityExposureResponse(
            new BigDecimal("150000.00"),
            Map.of("SINGAPORE", new BigDecimal("100000.00"), "HONG_KONG", new BigDecimal("50000.00")),
            Map.of("MOTOR", new BigDecimal("70000.00"), "PROPERTY", new BigDecimal("80000.00"))
        );

        when(dashboardService.getLiabilityExposure()).thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboard/liability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOutstandingLiability").value(150000.00))
                .andExpect(jsonPath("$.marketExposure.SINGAPORE").value(100000.00));
    }
}
