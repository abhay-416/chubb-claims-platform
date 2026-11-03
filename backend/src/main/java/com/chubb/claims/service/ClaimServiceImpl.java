package com.chubb.claims.service;

import com.chubb.claims.api.dto.*;
import com.chubb.claims.api.exception.InvalidStateTransitionException;
import com.chubb.claims.api.exception.ResourceNotFoundException;
import com.chubb.claims.api.exception.UnauthorizedActionException;
import com.chubb.claims.domain.*;
import com.chubb.claims.infrastructure.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
public class ClaimServiceImpl implements ClaimService {

    private final ClaimRepository claimRepository;
    private final ClaimHistoryRepository historyRepository;
    private final ClaimInfoRequestRepository infoRequestRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final EventPublisher eventPublisher;

    public ClaimServiceImpl(ClaimRepository claimRepository,
                            ClaimHistoryRepository historyRepository,
                            ClaimInfoRequestRepository infoRequestRepository,
                            UserRepository userRepository,
                            AuthService authService,
                            EventPublisher eventPublisher) {
        this.claimRepository = claimRepository;
        this.historyRepository = historyRepository;
        this.infoRequestRepository = infoRequestRepository;
        this.userRepository = userRepository;
        this.authService = authService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ClaimResponse submitClaim(CreateClaimRequest request) {
        User currentUser = authService.getCurrentAuthenticatedUser();

        Claim claim = new Claim();
        claim.setClaimNumber(generateClaimNumber(request.market()));
        claim.setPolicyNumber(request.policyNumber());
        claim.setClaimType(request.claimType());
        claim.setMarket(request.market());
        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setIncidentDate(request.incidentDate());
        claim.setIncidentDescription(request.incidentDescription());
        claim.setEstimatedAmount(request.estimatedAmount());
        claim.setClaimant(currentUser);

        Claim savedClaim = claimRepository.save(claim);

        // Record Initial Audit History
        ClaimHistory history = new ClaimHistory(savedClaim, null, ClaimStatus.SUBMITTED,
                currentUser.getEmail(), "First Notice of Loss (FNOL) submitted by claimant.");
        historyRepository.save(history);

        // Publish Asynchronous Event
        eventPublisher.publish(DomainEvents.ClaimEvent.created(savedClaim, currentUser.getEmail()));

        return ClaimResponse.fromEntity(savedClaim);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaimResponse> listClaims(ClaimStatus status, Market market, ClaimType type, Boolean unassignedOnly) {
        User currentUser = authService.getCurrentAuthenticatedUser();

        List<Claim> claims;
        if (currentUser.getRole() == UserRole.CLAIMANT) {
            // Claimants only view their own claims
            claims = claimRepository.findByClaimantId(currentUser.getId());
        } else {
            // Claims Officers and Managers can view and filter across the organization
            if (Boolean.TRUE.equals(unassignedOnly)) {
                claims = claimRepository.findByAssignedOfficerIsNull();
            } else {
                claims = claimRepository.filterClaims(status, market, null, null);
            }
        }

        if (type != null) {
            claims = claims.stream().filter(c -> c.getClaimType() == type).toList();
        }

        return claims.stream().map(ClaimResponse::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClaimDetailResponse getClaimDetail(Long id) {
        Claim claim = getClaimOrThrow(id);
        User currentUser = authService.getCurrentAuthenticatedUser();

        // Security check: Claimant can only access their own claims
        if (currentUser.getRole() == UserRole.CLAIMANT && !claim.getClaimant().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("You are not authorized to view claims belonging to another policyholder.");
        }

        List<ClaimHistoryItem> history = historyRepository.findByClaimIdOrderByTimestampDesc(id)
                .stream().map(ClaimHistoryItem::fromEntity).toList();

        List<ClaimInfoRequestItem> infoRequests = infoRequestRepository.findByClaimIdOrderByCreatedAtDesc(id)
                .stream().map(ClaimInfoRequestItem::fromEntity).toList();

        return new ClaimDetailResponse(ClaimResponse.fromEntity(claim), history, infoRequests);
    }

    @Override
    public ClaimResponse assignClaim(Long id, Long officerId) {
        Claim claim = getClaimOrThrow(id);
        User currentUser = authService.getCurrentAuthenticatedUser();

        // Enforce State Machine Transition: SUBMITTED -> ASSIGNED
        ClaimStateMachine.validateTransition(claim.getStatus(), ClaimStatus.ASSIGNED);

        User officerToAssign;
        if (officerId != null) {
            officerToAssign = userRepository.findById(officerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Officer not found with id: " + officerId));
        } else {
            // Self-assignment
            officerToAssign = currentUser;
        }

        ClaimStatus oldStatus = claim.getStatus();
        claim.setStatus(ClaimStatus.ASSIGNED);
        claim.setAssignedOfficer(officerToAssign);
        Claim updatedClaim = claimRepository.save(claim);

        historyRepository.save(new ClaimHistory(updatedClaim, oldStatus, ClaimStatus.ASSIGNED,
                currentUser.getEmail(), "Claim assigned to claims officer: " + officerToAssign.getName()));

        eventPublisher.publish(DomainEvents.ClaimEvent.assigned(updatedClaim, officerToAssign.getName()));

        return ClaimResponse.fromEntity(updatedClaim);
    }

    @Override
    public ClaimResponse startReview(Long id) {
        Claim claim = getClaimOrThrow(id);
        User currentUser = authService.getCurrentAuthenticatedUser();

        ClaimStateMachine.validateTransition(claim.getStatus(), ClaimStatus.UNDER_REVIEW);

        ClaimStatus oldStatus = claim.getStatus();
        claim.setStatus(ClaimStatus.UNDER_REVIEW);
        Claim updatedClaim = claimRepository.save(claim);

        historyRepository.save(new ClaimHistory(updatedClaim, oldStatus, ClaimStatus.UNDER_REVIEW,
                currentUser.getEmail(), "Investigation and damage assessment initiated."));

        eventPublisher.publish(DomainEvents.ClaimEvent.statusChanged(updatedClaim, oldStatus, currentUser.getEmail()));

        return ClaimResponse.fromEntity(updatedClaim);
    }

    @Override
    public ClaimResponse requestAdditionalInfo(Long id, RequestInfoPayload payload) {
        Claim claim = getClaimOrThrow(id);
        User currentUser = authService.getCurrentAuthenticatedUser();

        ClaimStateMachine.validateTransition(claim.getStatus(), ClaimStatus.ADDITIONAL_INFO_REQUIRED);

        ClaimStatus oldStatus = claim.getStatus();
        claim.setStatus(ClaimStatus.ADDITIONAL_INFO_REQUIRED);
        Claim updatedClaim = claimRepository.save(claim);

        ClaimInfoRequest infoRequest = new ClaimInfoRequest(updatedClaim, payload.requestedInfo(), currentUser.getName());
        infoRequestRepository.save(infoRequest);

        historyRepository.save(new ClaimHistory(updatedClaim, oldStatus, ClaimStatus.ADDITIONAL_INFO_REQUIRED,
                currentUser.getEmail(), "Requested additional documentation: " + payload.requestedInfo()));

        eventPublisher.publish(DomainEvents.ClaimEvent.statusChanged(updatedClaim, oldStatus, currentUser.getEmail()));

        return ClaimResponse.fromEntity(updatedClaim);
    }

    @Override
    public ClaimResponse submitAdditionalInfo(Long id, ProvideInfoPayload payload) {
        Claim claim = getClaimOrThrow(id);
        User currentUser = authService.getCurrentAuthenticatedUser();

        ClaimStateMachine.validateTransition(claim.getStatus(), ClaimStatus.UNDER_REVIEW);

        // Update pending requests
        List<ClaimInfoRequest> pendingRequests = infoRequestRepository.findByClaimIdOrderByCreatedAtDesc(id)
                .stream().filter(r -> "PENDING".equals(r.getStatus())).toList();

        for (ClaimInfoRequest req : pendingRequests) {
            req.setResponse(payload.response());
            req.setRespondedAt(LocalDateTime.now());
            req.setStatus("RESOLVED");
            infoRequestRepository.save(req);
        }

        ClaimStatus oldStatus = claim.getStatus();
        claim.setStatus(ClaimStatus.UNDER_REVIEW);
        Claim updatedClaim = claimRepository.save(claim);

        historyRepository.save(new ClaimHistory(updatedClaim, oldStatus, ClaimStatus.UNDER_REVIEW,
                currentUser.getEmail(), "Additional information supplied by policyholder. Resumed assessment."));

        eventPublisher.publish(DomainEvents.ClaimEvent.statusChanged(updatedClaim, oldStatus, currentUser.getEmail()));

        return ClaimResponse.fromEntity(updatedClaim);
    }

    @Override
    public ClaimResponse assessClaim(Long id, AssessClaimRequest request) {
        Claim claim = getClaimOrThrow(id);
        User currentUser = authService.getCurrentAuthenticatedUser();

        ClaimStatus targetStatus = "APPROVE".equalsIgnoreCase(request.decision())
                ? ClaimStatus.APPROVED : ClaimStatus.REJECTED;

        ClaimStateMachine.validateTransition(claim.getStatus(), targetStatus);

        if (targetStatus == ClaimStatus.APPROVED) {
            if (request.approvedAmount() == null || request.approvedAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidStateTransitionException("Approved amount must be specified and greater than zero.");
            }
            claim.setApprovedAmount(request.approvedAmount());
        }

        ClaimStatus oldStatus = claim.getStatus();
        claim.setStatus(targetStatus);
        Claim updatedClaim = claimRepository.save(claim);

        String notes = request.notes() != null ? request.notes() : "Claim " + targetStatus.name().toLowerCase() + " by officer.";
        historyRepository.save(new ClaimHistory(updatedClaim, oldStatus, targetStatus, currentUser.getEmail(), notes));

        eventPublisher.publish(DomainEvents.ClaimEvent.statusChanged(updatedClaim, oldStatus, currentUser.getEmail()));

        return ClaimResponse.fromEntity(updatedClaim);
    }

    @Override
    public ClaimResponse settleClaim(Long id, String settlementNotes) {
        Claim claim = getClaimOrThrow(id);
        User currentUser = authService.getCurrentAuthenticatedUser();

        ClaimStateMachine.validateTransition(claim.getStatus(), ClaimStatus.SETTLED);

        ClaimStatus oldStatus = claim.getStatus();
        claim.setStatus(ClaimStatus.SETTLED);
        Claim updatedClaim = claimRepository.save(claim);

        String notes = settlementNotes != null ? settlementNotes : "Claim settlement and payout finalized.";
        historyRepository.save(new ClaimHistory(updatedClaim, oldStatus, ClaimStatus.SETTLED, currentUser.getEmail(), notes));

        eventPublisher.publish(DomainEvents.ClaimEvent.settled(updatedClaim, currentUser.getEmail()));

        return ClaimResponse.fromEntity(updatedClaim);
    }

    private Claim getClaimOrThrow(Long id) {
        return claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + id));
    }

    private String generateClaimNumber(Market market) {
        String code = switch (market) {
            case SINGAPORE -> "SG";
            case HONG_KONG -> "HK";
            case AUSTRALIA -> "AU";
            case THAILAND -> "TH";
            case MALAYSIA -> "MY";
            case INDONESIA -> "ID";
        };
        int randomDigits = ThreadLocalRandom.current().nextInt(1000, 9999);
        return String.format("CLM-%s-%d-%04d", code, Year.now().getValue(), randomDigits);
    }
}
