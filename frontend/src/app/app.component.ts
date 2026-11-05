import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClaimsService } from './shared/services/claims.service';
import { Claim, ClaimDetail, DashboardMetrics, LiabilityExposure, User } from './core/models/claim.model';
import { ClaimantComponent } from './features/claimant/claimant.component';
import { OfficerComponent } from './features/officer/officer.component';
import { ManagerComponent } from './features/manager/manager.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, ClaimantComponent, OfficerComponent, ManagerComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  activeRole: 'CLAIMANT' | 'CLAIMS_OFFICER' | 'CLAIMS_MANAGER' = 'CLAIMS_OFFICER';

  currentUser: User = {
    id: 2,
    name: 'Sarah Jenkins',
    email: 'officer@chubb.com',
    role: 'CLAIMS_OFFICER',
    market: 'SINGAPORE'
  };

  claims: Claim[] = [];
  selectedClaimDetail: ClaimDetail | null = null;
  metrics: DashboardMetrics | null = null;
  liability: LiabilityExposure | null = null;

  activeTab: 'main' | 'detail' = 'main';
  actionFeedback: string = '';
  errorMessage: string = '';

  // Detail Modal Actions
  infoRequestText: string = '';
  infoResponseText: string = '';
  approvalAmount: number = 0;
  assessmentNotes: string = '';
  settlementNotes: string = '';

  constructor(public claimsService: ClaimsService) {}

  ngOnInit() {
    this.switchPersona(this.activeRole);
  }

  toggleDemoMode() {
    this.claimsService.setDemoMode(!this.claimsService.isDemoMode);
    this.actionFeedback = this.claimsService.isDemoMode
      ? 'Demo Mode Activated: Using local offline fixtures for review.'
      : 'Live Mode Activated: Connected directly to Spring Boot REST backend.';
    this.loadDashboardData();
  }

  switchPersona(role: 'CLAIMANT' | 'CLAIMS_OFFICER' | 'CLAIMS_MANAGER') {
    this.activeRole = role;
    this.activeTab = 'main';
    this.selectedClaimDetail = null;

    if (role === 'CLAIMANT') {
      this.currentUser = { id: 1, name: 'Tan Wei Ling', email: 'claimant.sg@gmail.com', role: 'CLAIMANT', market: 'SINGAPORE' };
    } else if (role === 'CLAIMS_OFFICER') {
      this.currentUser = { id: 2, name: 'Sarah Jenkins', email: 'officer@chubb.com', role: 'CLAIMS_OFFICER', market: 'SINGAPORE' };
    } else {
      this.currentUser = { id: 3, name: 'Marcus Vance', email: 'manager@chubb.com', role: 'CLAIMS_MANAGER', market: 'SINGAPORE' };
    }

    this.claimsService.login(this.currentUser.email, 'password123').subscribe({
      next: (res) => {
        if (res && res.token) {
          this.claimsService.setToken(res.token);
        }
        this.loadDashboardData();
      },
      error: (err) => {
        // Explicitly report error if live mode, or offer demo mode switch
        this.errorMessage = `Could not authenticate against live backend (${err.status || 'Offline'}). Toggle Demo Mode if backend is not running.`;
        this.loadDashboardData();
      }
    });
  }

  loadDashboardData(filters?: { market: string; status: string }) {
    this.errorMessage = '';
    const m = filters?.market || '';
    const s = filters?.status || '';

    this.claimsService.getClaims(s, m).subscribe({
      next: (data) => this.claims = data,
      error: (err) => {
        this.errorMessage = `Failed to load claims from backend: ${err.message || 'Connection refused'}. You can enable 'Demo Mode' above.`;
      }
    });

    if (this.activeRole === 'CLAIMS_MANAGER' || this.activeRole === 'CLAIMS_OFFICER') {
      this.claimsService.getMetrics().subscribe({
        next: (res) => this.metrics = res,
        error: () => {}
      });
      this.claimsService.getLiabilityExposure().subscribe({
        next: (res) => this.liability = res,
        error: () => {}
      });
    }
  }

  onClaimSubmitted(payload: any) {
    this.claimsService.submitClaim(payload).subscribe({
      next: (created) => {
        this.actionFeedback = `Claim successfully submitted! Reference ID: ${created.claimNumber}`;
        this.loadDashboardData();
      },
      error: (err) => {
        this.errorMessage = 'Claim submission failed: ' + (err.error?.message || err.message);
      }
    });
  }

  viewClaimDetail(id: number) {
    this.claimsService.getClaimDetail(id).subscribe({
      next: (detail) => {
        this.selectedClaimDetail = detail;
        this.approvalAmount = detail.claim.estimatedAmount;
        this.activeTab = 'detail';
        this.actionFeedback = '';
      },
      error: (err) => {
        this.errorMessage = 'Failed to load claim detail: ' + (err.error?.message || err.message);
      }
    });
  }

  // Lifecycle State Machine Triggers
  assignToMe(id: number) {
    this.claimsService.assignClaim(id, this.currentUser.id).subscribe({
      next: () => {
        this.actionFeedback = 'Claim assigned to you. Status updated to ASSIGNED.';
        this.viewClaimDetail(id);
        this.loadDashboardData();
      },
      error: (err) => this.errorMessage = err.error?.message || err.message
    });
  }

  startReview(id: number) {
    this.claimsService.startReview(id).subscribe({
      next: () => {
        this.actionFeedback = 'Investigation started. Status updated to UNDER_REVIEW.';
        this.viewClaimDetail(id);
        this.loadDashboardData();
      },
      error: (err) => this.errorMessage = err.error?.message || err.message
    });
  }

  sendInfoRequest(id: number) {
    if (!this.infoRequestText) return;
    this.claimsService.requestInfo(id, this.infoRequestText).subscribe({
      next: () => {
        this.actionFeedback = 'Request dispatched to claimant. Status updated to ADDITIONAL_INFO_REQUIRED.';
        this.infoRequestText = '';
        this.viewClaimDetail(id);
        this.loadDashboardData();
      },
      error: (err) => this.errorMessage = err.error?.message || err.message
    });
  }

  sendInfoResponse(id: number) {
    if (!this.infoResponseText) return;
    this.claimsService.submitInfo(id, this.infoResponseText).subscribe({
      next: () => {
        this.actionFeedback = 'Information supplied. Investigation resumed (UNDER_REVIEW).';
        this.infoResponseText = '';
        this.viewClaimDetail(id);
        this.loadDashboardData();
      },
      error: (err) => this.errorMessage = err.error?.message || err.message
    });
  }

  assessClaim(id: number, decision: 'APPROVE' | 'REJECT') {
    this.claimsService.assessClaim(id, decision, this.approvalAmount, this.assessmentNotes).subscribe({
      next: () => {
        this.actionFeedback = `Claim assessment completed: ${decision}.`;
        this.assessmentNotes = '';
        this.viewClaimDetail(id);
        this.loadDashboardData();
      },
      error: (err) => this.errorMessage = err.error?.message || err.message
    });
  }

  settleClaim(id: number) {
    this.claimsService.settleClaim(id, this.settlementNotes).subscribe({
      next: () => {
        this.actionFeedback = 'Claim payout finalized and settled. Financial ledger updated.';
        this.settlementNotes = '';
        this.viewClaimDetail(id);
        this.loadDashboardData();
      },
      error: (err) => this.errorMessage = err.error?.message || err.message
    });
  }
}
