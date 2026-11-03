import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClaimsService, Claim, ClaimDetail, DashboardMetrics, LiabilityExposure, User } from './shared/claims.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  // Current Selected Persona
  activeRole: 'CLAIMANT' | 'CLAIMS_OFFICER' | 'CLAIMS_MANAGER' = 'CLAIMS_OFFICER';
  currentUser: User = {
    id: 2,
    name: 'Sarah Jenkins',
    email: 'officer@chubb.com',
    role: 'CLAIMS_OFFICER',
    market: 'SINGAPORE'
  };

  // UI Navigation
  activeTab: string = 'queue'; // 'queue', 'submit', 'detail', 'manager'

  // Data Collections
  claims: Claim[] = [];
  selectedClaimDetail: ClaimDetail | null = null;
  metrics: DashboardMetrics | null = null;
  liability: LiabilityExposure | null = null;

  // Filters
  filterMarket: string = '';
  filterStatus: string = '';

  // FNOL Form Model
  newClaim = {
    policyNumber: '',
    claimType: 'MOTOR',
    market: 'SINGAPORE',
    incidentDate: new Date().toISOString().split('T')[0],
    incidentDescription: '',
    estimatedAmount: 2500.00
  };

  // Action Modals & Inputs
  infoRequestText: string = '';
  infoResponseText: string = '';
  approvalAmount: number = 0;
  assessmentNotes: string = '';
  settlementNotes: string = '';
  actionFeedback: string = '';

  constructor(private claimsService: ClaimsService) {}

  ngOnInit() {
    this.switchPersona(this.activeRole);
  }

  switchPersona(role: 'CLAIMANT' | 'CLAIMS_OFFICER' | 'CLAIMS_MANAGER') {
    this.activeRole = role;
    if (role === 'CLAIMANT') {
      this.currentUser = { id: 1, name: 'Tan Wei Ling', email: 'claimant.sg@gmail.com', role: 'CLAIMANT', market: 'SINGAPORE' };
      this.activeTab = 'my-claims';
    } else if (role === 'CLAIMS_OFFICER') {
      this.currentUser = { id: 2, name: 'Sarah Jenkins', email: 'officer@chubb.com', role: 'CLAIMS_OFFICER', market: 'SINGAPORE' };
      this.activeTab = 'queue';
    } else {
      this.currentUser = { id: 3, name: 'Marcus Vance', email: 'manager@chubb.com', role: 'CLAIMS_MANAGER', market: 'SINGAPORE' };
      this.activeTab = 'manager';
    }

    // Attempt JWT Login
    this.claimsService.login(this.currentUser.email, 'password123').subscribe({
      next: (res) => {
        if (res && res.token) {
          this.claimsService.setToken(res.token);
        }
        this.loadDashboardData();
      },
      error: () => {
        this.loadDashboardData();
      }
    });
  }

  loadDashboardData() {
    this.claimsService.getClaims(this.filterStatus, this.filterMarket).subscribe(data => {
      this.claims = data;
    });

    if (this.activeRole === 'CLAIMS_MANAGER' || this.activeRole === 'CLAIMS_OFFICER') {
      this.claimsService.getMetrics().subscribe(m => this.metrics = m);
      this.claimsService.getLiabilityExposure().subscribe(l => this.liability = l);
    }
  }

  viewClaimDetail(id: number) {
    this.claimsService.getClaimDetail(id).subscribe(detail => {
      this.selectedClaimDetail = detail;
      this.approvalAmount = detail.claim.estimatedAmount;
      this.activeTab = 'detail';
      this.actionFeedback = '';
    });
  }

  submitNewClaim() {
    if (!this.newClaim.policyNumber || !this.newClaim.incidentDescription) {
      alert('Please fill in policy number and incident description.');
      return;
    }

    this.claimsService.submitClaim(this.newClaim).subscribe({
      next: (created) => {
        this.actionFeedback = `Claim successfully submitted! Assigned ID: ${created.claimNumber}`;
        this.newClaim.policyNumber = '';
        this.newClaim.incidentDescription = '';
        this.loadDashboardData();
        this.activeTab = 'my-claims';
      },
      error: (err) => {
        alert('Failed to submit claim: ' + (err.error?.message || err.message));
      }
    });
  }

  // Claim Lifecycle Actions
  assignToMe(id: number) {
    this.claimsService.assignClaim(id, this.currentUser.id).subscribe(() => {
      this.actionFeedback = 'Claim assigned to you. Status updated to ASSIGNED.';
      this.viewClaimDetail(id);
      this.loadDashboardData();
    });
  }

  startReview(id: number) {
    this.claimsService.startReview(id).subscribe(() => {
      this.actionFeedback = 'Investigation started. Status updated to UNDER_REVIEW.';
      this.viewClaimDetail(id);
      this.loadDashboardData();
    });
  }

  sendInfoRequest(id: number) {
    if (!this.infoRequestText) return;
    this.claimsService.requestInfo(id, this.infoRequestText).subscribe(() => {
      this.actionFeedback = 'Request sent to claimant. Status updated to ADDITIONAL_INFO_REQUIRED.';
      this.infoRequestText = '';
      this.viewClaimDetail(id);
      this.loadDashboardData();
    });
  }

  sendInfoResponse(id: number) {
    if (!this.infoResponseText) return;
    this.claimsService.submitInfo(id, this.infoResponseText).subscribe(() => {
      this.actionFeedback = 'Information submitted. Investigation resumed (UNDER_REVIEW).';
      this.infoResponseText = '';
      this.viewClaimDetail(id);
      this.loadDashboardData();
    });
  }

  assessClaim(id: number, decision: 'APPROVE' | 'REJECT') {
    this.claimsService.assessClaim(id, decision, this.approvalAmount, this.assessmentNotes).subscribe(() => {
      this.actionFeedback = `Claim assessment completed: ${decision}.`;
      this.assessmentNotes = '';
      this.viewClaimDetail(id);
      this.loadDashboardData();
    });
  }

  settleClaim(id: number) {
    this.claimsService.settleClaim(id, this.settlementNotes).subscribe(() => {
      this.actionFeedback = 'Claim finalized and settled. Financial payout recorded.';
      this.settlementNotes = '';
      this.viewClaimDetail(id);
      this.loadDashboardData();
    });
  }

  formatMarket(market: string): string {
    return market ? market.replace('_', ' ') : '';
  }
}
