package com.chubb.claims.claim.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "claim_history")
public class ClaimHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 50)
    private ClaimStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 50)
    private ClaimStatus newStatus;

    @Column(name = "changed_by", nullable = false, length = 100)
    private String changedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public ClaimHistory() {}

    public ClaimHistory(Claim claim, ClaimStatus oldStatus, ClaimStatus newStatus, String changedBy, String notes) {
        this.claim = claim;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.notes = notes;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Claim getClaim() { return claim; }
    public void setClaim(Claim claim) { this.claim = claim; }

    public ClaimStatus getOldStatus() { return oldStatus; }
    public void setOldStatus(ClaimStatus oldStatus) { this.oldStatus = oldStatus; }

    public ClaimStatus getNewStatus() { return newStatus; }
    public void setNewStatus(ClaimStatus newStatus) { this.newStatus = newStatus; }

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
