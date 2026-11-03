package com.chubb.claims.service;

import com.chubb.claims.api.dto.AssessClaimRequest;
import com.chubb.claims.api.dto.ClaimResponse;
import com.chubb.claims.api.dto.CreateClaimRequest;
import com.chubb.claims.api.exception.InvalidStateTransitionException;
import com.chubb.claims.domain.*;
import com.chubb.claims.infrastructure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Claim Service Workflow Unit Tests")
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ClaimHistoryRepository historyRepository;

    @Mock
    private ClaimInfoRequestRepository infoRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private ClaimServiceImpl claimService;

    private User claimant;
    private User officer;
    private Claim claim;

    @BeforeEach
    void setUp() {
        claimant = new User("Tan Wei Ling", "claimant@gmail.com", "pass", UserRole.CLAIMANT, Market.SINGAPORE);
        claimant.setId(1L);

        officer = new User("Sarah Jenkins", "officer@chubb.com", "pass", UserRole.CLAIMS_OFFICER, Market.SINGAPORE);
        officer.setId(2L);

        claim = new Claim();
        claim.setId(10L);
        claim.setClaimNumber("CLM-SG-2026-9999");
        claim.setPolicyNumber("POL-SG-99");
        claim.setClaimType(ClaimType.MOTOR);
        claim.setMarket(Market.SINGAPORE);
        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setIncidentDate(LocalDate.now());
        claim.setIncidentDescription("Motor collision");
        claim.setEstimatedAmount(new BigDecimal("5000.00"));
        claim.setClaimant(claimant);
    }

    @Test
    @DisplayName("Should successfully submit FNOL claim with SUBMITTED status and emit event")
    void shouldSubmitClaimSuccessfully() {
        when(authService.getCurrentAuthenticatedUser()).thenReturn(claimant);
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> {
            Claim c = invocation.getArgument(0);
            c.setId(10L);
            return c;
        });

        CreateClaimRequest request = new CreateClaimRequest(
            "POL-SG-99", ClaimType.MOTOR, Market.SINGAPORE, LocalDate.now(),
            "Motor collision", new BigDecimal("5000.00")
        );

        ClaimResponse response = claimService.submitClaim(request);

        assertNotNull(response);
        assertEquals(ClaimStatus.SUBMITTED, response.status());
        assertEquals("POL-SG-99", response.policyNumber());
        assertEquals(new BigDecimal("5000.00"), response.estimatedAmount());

        verify(historyRepository, times(1)).save(any(ClaimHistory.class));
        verify(eventPublisher, times(1)).publish(any(DomainEvents.ClaimEvent.class));
    }

    @Test
    @DisplayName("Should assign claim to officer and transition SUBMITTED -> ASSIGNED")
    void shouldAssignClaimSuccessfully() {
        when(claimRepository.findById(10L)).thenReturn(Optional.of(claim));
        when(authService.getCurrentAuthenticatedUser()).thenReturn(officer);
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClaimResponse response = claimService.assignClaim(10L, null);

        assertEquals(ClaimStatus.ASSIGNED, response.status());
        assertEquals(officer.getId(), response.assignedOfficerId());
        verify(eventPublisher, times(1)).publish(any(DomainEvents.ClaimEvent.class));
    }

    @Test
    @DisplayName("Should reject illegal state transition (e.g. SUBMITTED directly to APPROVED)")
    void shouldRejectDirectApprovalFromSubmitted() {
        when(claimRepository.findById(10L)).thenReturn(Optional.of(claim));
        when(authService.getCurrentAuthenticatedUser()).thenReturn(officer);

        AssessClaimRequest request = new AssessClaimRequest("APPROVE", new BigDecimal("4000.00"), "Direct approval");

        assertThrows(InvalidStateTransitionException.class, () ->
            claimService.assessClaim(10L, request)
        );

        verify(claimRepository, never()).save(any(Claim.class));
    }

    @Test
    @DisplayName("Should approve claim when in UNDER_REVIEW status")
    void shouldApproveClaimFromUnderReview() {
        claim.setStatus(ClaimStatus.UNDER_REVIEW);
        when(claimRepository.findById(10L)).thenReturn(Optional.of(claim));
        when(authService.getCurrentAuthenticatedUser()).thenReturn(officer);
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AssessClaimRequest request = new AssessClaimRequest("APPROVE", new BigDecimal("4800.00"), "Damage assessed and verified.");
        ClaimResponse response = claimService.assessClaim(10L, request);

        assertEquals(ClaimStatus.APPROVED, response.status());
        assertEquals(new BigDecimal("4800.00"), response.approvedAmount());
    }

    @Test
    @DisplayName("Should settle approved claim and disburse funds")
    void shouldSettleApprovedClaim() {
        claim.setStatus(ClaimStatus.APPROVED);
        claim.setApprovedAmount(new BigDecimal("4800.00"));
        when(claimRepository.findById(10L)).thenReturn(Optional.of(claim));
        when(authService.getCurrentAuthenticatedUser()).thenReturn(officer);
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClaimResponse response = claimService.settleClaim(10L, "Payment cleared via Fast Pay.");

        assertEquals(ClaimStatus.SETTLED, response.status());
        verify(eventPublisher, times(1)).publish(any(DomainEvents.ClaimEvent.class));
    }
}
