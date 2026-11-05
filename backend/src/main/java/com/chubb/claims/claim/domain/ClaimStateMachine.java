package com.chubb.claims.claim.domain;

import com.chubb.claims.shared.exception.InvalidStateTransitionException;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * State Machine defining valid claim lifecycle transitions according to
 * Chubb APAC Insurance Underwriting & Processing rules.
 *
 * Lifecycle:
 * 1. SUBMITTED -> ASSIGNED
 * 2. ASSIGNED -> UNDER_REVIEW
 * 3. UNDER_REVIEW -> ADDITIONAL_INFO_REQUIRED, APPROVED, REJECTED
 * 4. ADDITIONAL_INFO_REQUIRED -> UNDER_REVIEW
 * 5. APPROVED -> SETTLED
 */
public class ClaimStateMachine {

    private static final Map<ClaimStatus, Set<ClaimStatus>> ALLOWED_TRANSITIONS = Map.of(
        ClaimStatus.SUBMITTED, EnumSet.of(ClaimStatus.ASSIGNED),
        ClaimStatus.ASSIGNED, EnumSet.of(ClaimStatus.UNDER_REVIEW),
        ClaimStatus.UNDER_REVIEW, EnumSet.of(ClaimStatus.ADDITIONAL_INFO_REQUIRED, ClaimStatus.APPROVED, ClaimStatus.REJECTED),
        ClaimStatus.ADDITIONAL_INFO_REQUIRED, EnumSet.of(ClaimStatus.UNDER_REVIEW),
        ClaimStatus.APPROVED, EnumSet.of(ClaimStatus.SETTLED),
        ClaimStatus.REJECTED, EnumSet.noneOf(ClaimStatus.class),
        ClaimStatus.SETTLED, EnumSet.noneOf(ClaimStatus.class)
    );

    public static void validateTransition(ClaimStatus currentStatus, ClaimStatus targetStatus) {
        if (currentStatus == null || targetStatus == null) {
            throw new InvalidStateTransitionException("Current and target statuses cannot be null.");
        }

        Set<ClaimStatus> validTargets = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, EnumSet.noneOf(ClaimStatus.class));
        if (!validTargets.contains(targetStatus)) {
            throw new InvalidStateTransitionException(
                String.format("Invalid state transition from %s to %s. Allowed transitions: %s",
                    currentStatus, targetStatus, validTargets)
            );
        }
    }

    public static boolean canTransition(ClaimStatus currentStatus, ClaimStatus targetStatus) {
        if (currentStatus == null || targetStatus == null) return false;
        Set<ClaimStatus> validTargets = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, EnumSet.noneOf(ClaimStatus.class));
        return validTargets.contains(targetStatus);
    }
}
