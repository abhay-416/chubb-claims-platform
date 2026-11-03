package com.chubb.claims.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "claim_info_requests")
public class ClaimInfoRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @Column(name = "requested_info", nullable = false, columnDefinition = "TEXT")
    private String requestedInfo;

    @Column(name = "requested_by", nullable = false, length = 100)
    private String requestedBy;

    @Column(columnDefinition = "TEXT")
    private String response;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(nullable = false, length = 30)
    private String status = "PENDING"; // PENDING, RESOLVED

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ClaimInfoRequest() {}

    public ClaimInfoRequest(Claim claim, String requestedInfo, String requestedBy) {
        this.claim = claim;
        this.requestedInfo = requestedInfo;
        this.requestedBy = requestedBy;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Claim getClaim() { return claim; }
    public void setClaim(Claim claim) { this.claim = claim; }

    public String getRequestedInfo() { return requestedInfo; }
    public void setRequestedInfo(String requestedInfo) { this.requestedInfo = requestedInfo; }

    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
