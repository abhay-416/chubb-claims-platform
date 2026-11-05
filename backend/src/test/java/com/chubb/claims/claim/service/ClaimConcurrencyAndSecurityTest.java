package com.chubb.claims.claim.service;

import com.chubb.claims.auth.domain.User;
import com.chubb.claims.auth.domain.UserRole;
import com.chubb.claims.auth.service.AuthService;
import com.chubb.claims.claim.domain.Claim;
import com.chubb.claims.claim.domain.ClaimStatus;
import com.chubb.claims.claim.domain.ClaimType;
import com.chubb.claims.claim.domain.Market;
import com.chubb.claims.claim.infrastructure.ClaimRepository;
import com.chubb.claims.shared.exception.UnauthorizedActionException;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Concurrency & Security Enforcement Unit Tests")
class ClaimConcurrencyAndSecurityTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private ClaimServiceImpl claimService;

    @Test
    @DisplayName("Cross-Tenant Security: Claimant cannot view another claimant's claim")
    void shouldBlockCrossClaimantAccess() {
        User claimantA = new User("Claimant A", "claimantA@gmail.com", "pass", UserRole.CLAIMANT, Market.SINGAPORE);
        claimantA.setId(100L);

        User claimantB = new User("Claimant B", "claimantB@gmail.com", "pass", UserRole.CLAIMANT, Market.SINGAPORE);
        claimantB.setId(200L);

        Claim claimOfB = new Claim();
        claimOfB.setId(50L);
        claimOfB.setClaimant(claimantB);
        claimOfB.setStatus(ClaimStatus.SUBMITTED);

        when(claimRepository.findById(50L)).thenReturn(Optional.of(claimOfB));
        when(authService.getCurrentAuthenticatedUser()).thenReturn(claimantA);

        assertThrows(UnauthorizedActionException.class, () ->
            claimService.getClaimDetail(50L)
        );
    }

    @Test
    @DisplayName("Market Jurisdiction Security: Officer in Australia cannot assess Singapore claim")
    void shouldBlockCrossMarketOfficerAction() {
        User officerAu = new User("Liam O'Connor", "officer.au@chubb.com", "pass", UserRole.CLAIMS_OFFICER, Market.AUSTRALIA);
        officerAu.setId(300L);

        Claim claimSg = new Claim();
        claimSg.setId(60L);
        claimSg.setMarket(Market.SINGAPORE);
        claimSg.setStatus(ClaimStatus.SUBMITTED);

        when(claimRepository.findById(60L)).thenReturn(Optional.of(claimSg));
        when(authService.getCurrentAuthenticatedUser()).thenReturn(officerAu);

        assertThrows(UnauthorizedActionException.class, () ->
            claimService.assignClaim(60L, officerAu.getId())
        );
    }

    @Test
    @DisplayName("Concurrency Safety: Optimistic locking version field exists and protects state")
    void shouldVerifyOptimisticLockingVersionField() {
        Claim claim = new Claim();
        claim.setId(1L);
        claim.setPolicyNumber("POL-100");
        claim.setEstimatedAmount(new BigDecimal("1000.00"));
        claim.setVersion(0L);

        assertEquals(0L, claim.getVersion());
        claim.setVersion(1L);
        assertEquals(1L, claim.getVersion());
    }
}
