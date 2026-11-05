package com.chubb.claims.claim.service;

import com.chubb.claims.claim.domain.ClaimStateMachine;
import com.chubb.claims.claim.domain.ClaimStatus;
import com.chubb.claims.shared.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Claim State Machine Lifecycle Unit Tests")
class ClaimStateMachineTest {

    @ParameterizedTest(name = "Valid transition from {0} to {1} should be permitted")
    @CsvSource({
        "SUBMITTED, ASSIGNED",
        "ASSIGNED, UNDER_REVIEW",
        "UNDER_REVIEW, ADDITIONAL_INFO_REQUIRED",
        "ADDITIONAL_INFO_REQUIRED, UNDER_REVIEW",
        "UNDER_REVIEW, APPROVED",
        "UNDER_REVIEW, REJECTED",
        "APPROVED, SETTLED"
    })
    void shouldAllowValidStateTransitions(ClaimStatus current, ClaimStatus target) {
        assertTrue(ClaimStateMachine.canTransition(current, target));
        assertDoesNotThrow(() -> ClaimStateMachine.validateTransition(current, target));
    }

    @ParameterizedTest(name = "Illegal transition from {0} to {1} must throw InvalidStateTransitionException")
    @CsvSource({
        "SUBMITTED, SETTLED",
        "SUBMITTED, APPROVED",
        "SUBMITTED, UNDER_REVIEW",
        "ASSIGNED, SETTLED",
        "ASSIGNED, APPROVED",
        "REJECTED, SETTLED",
        "REJECTED, UNDER_REVIEW",
        "SETTLED, SUBMITTED",
        "SETTLED, APPROVED",
        "ADDITIONAL_INFO_REQUIRED, APPROVED",
        "ADDITIONAL_INFO_REQUIRED, SETTLED"
    })
    void shouldRejectIllegalStateTransitions(ClaimStatus current, ClaimStatus target) {
        assertFalse(ClaimStateMachine.canTransition(current, target));
        assertThrows(InvalidStateTransitionException.class, () ->
            ClaimStateMachine.validateTransition(current, target)
        );
    }

    @Test
    void shouldRejectNullStatuses() {
        assertThrows(InvalidStateTransitionException.class, () ->
            ClaimStateMachine.validateTransition(null, ClaimStatus.ASSIGNED)
        );
        assertThrows(InvalidStateTransitionException.class, () ->
            ClaimStateMachine.validateTransition(ClaimStatus.SUBMITTED, null)
        );
    }
}
