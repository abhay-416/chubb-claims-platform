import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Claim, ClaimDetail } from '../../core/models/claim.model';
import { ClaimsService } from '../../shared/services/claims.service';

@Component({
  selector: 'app-claimant-view',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="claimant-container">
      <div class="tab-bar">
        <button [class.tab-active]="subTab === 'list'" (click)="subTab = 'list'">My Insurance Claims</button>
        <button [class.tab-active]="subTab === 'new'" (click)="subTab = 'new'">+ Report Incident (FNOL)</button>
      </div>

      <!-- FNOL Form -->
      <div *ngIf="subTab === 'new'" class="card form-card">
        <h3>Report First Notice of Loss (FNOL)</h3>
        <p class="subtitle">Submit incident details for motor or property claims in APAC</p>

        <form (ngSubmit)="submitFNOL()">
          <div class="form-row">
            <div class="form-group">
              <label>Policy Number *</label>
              <input type="text" [(ngModel)]="fnol.policyNumber" name="policyNumber" placeholder="e.g. POL-SG-MOT-9821" required>
            </div>
            <div class="form-group">
              <label>Portfolio Type *</label>
              <select [(ngModel)]="fnol.claimType" name="claimType">
                <option value="MOTOR">Motor Insurance</option>
                <option value="PROPERTY">Property & Casualty</option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Market Jurisdiction *</label>
              <select [(ngModel)]="fnol.market" name="market">
                <option value="SINGAPORE">Singapore (SGD)</option>
                <option value="HONG_KONG">Hong Kong (HKD)</option>
                <option value="AUSTRALIA">Australia (AUD)</option>
                <option value="THAILAND">Thailand (THB)</option>
                <option value="MALAYSIA">Malaysia (MYR)</option>
                <option value="INDONESIA">Indonesia (IDR)</option>
              </select>
            </div>
            <div class="form-group">
              <label>Incident Date *</label>
              <input type="date" [(ngModel)]="fnol.incidentDate" name="incidentDate" required>
            </div>
          </div>

          <div class="form-group">
            <label>Estimated Loss Amount *</label>
            <input type="number" step="0.01" [(ngModel)]="fnol.estimatedAmount" name="estimatedAmount" required>
          </div>

          <div class="form-group">
            <label>Incident Circumstances & Observed Damage *</label>
            <textarea rows="4" [(ngModel)]="fnol.incidentDescription" name="incidentDescription"
                      placeholder="Describe what occurred, location, third party details and damage extent..." required></textarea>
          </div>

          <div class="form-actions">
            <button type="button" class="btn-secondary" (click)="subTab = 'list'">Cancel</button>
            <button type="submit" class="btn-primary">Submit First Notice of Loss</button>
          </div>
        </form>
      </div>

      <!-- Claims List -->
      <div *ngIf="subTab === 'list'" class="card table-card">
        <h3>My Submitted Claims</h3>
        <table class="claims-table">
          <thead>
            <tr>
              <th>Claim Number</th>
              <th>Policy #</th>
              <th>Type</th>
              <th>Market</th>
              <th>Estimated Loss</th>
              <th>Approved Payout</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let c of claims">
              <td class="bold-cell">{{ c.claimNumber }}</td>
              <td>{{ c.policyNumber }}</td>
              <td><span class="type-tag">{{ c.claimType }}</span></td>
              <td>{{ c.market }} ({{ c.currency }})</td>
              <td>{{ c.currency }} {{ c.estimatedAmount | number:'1.2-2' }}</td>
              <td>{{ c.approvedAmount ? (c.currency + ' ' + (c.approvedAmount | number:'1.2-2')) : '-' }}</td>
              <td>
                <span class="badge" [ngClass]="'badge-' + c.status.toLowerCase()">{{ c.status.replace('_', ' ') }}</span>
              </td>
              <td>
                <button class="btn-inspect" (click)="selectClaim.emit(c.id)">Track Progress</button>
              </td>
            </tr>
            <tr *ngIf="claims.length === 0">
              <td colspan="8" class="empty-state">No claims submitted yet. Click '+ Report Incident' to file an FNOL.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .claimant-container { display: flex; flex-direction: column; gap: 1.5rem; }
    .tab-bar { display: flex; gap: 0.75rem; }
    .tab-bar button {
      background: #ffffff; border: 1px solid #e2e8f0; padding: 0.5rem 1.25rem;
      border-radius: 6px; font-weight: 600; cursor: pointer;
    }
    .tab-bar button.tab-active { background: #002b49; color: #ffffff; border-color: #002b49; }
    .card { background: #ffffff; border: 1px solid #e2e8f0; border-radius: 8px; padding: 1.5rem; }
    .form-card { max-width: 800px; margin: 0 auto; }
    .subtitle { color: #64748b; font-size: 0.85rem; margin-bottom: 1.25rem; }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
    .form-group { display: flex; flex-direction: column; margin-bottom: 1rem; }
    .form-group label { font-size: 0.8rem; font-weight: 600; margin-bottom: 0.35rem; }
    input, select, textarea { padding: 0.65rem; border: 1px solid #e2e8f0; border-radius: 6px; }
    .form-actions { display: flex; justify-content: flex-end; gap: 1rem; margin-top: 1rem; }
    .btn-primary { background: #002b49; color: #ffffff; border: none; padding: 0.65rem 1.25rem; border-radius: 6px; font-weight: 600; cursor: pointer; }
    .btn-secondary { background: #ffffff; border: 1px solid #e2e8f0; padding: 0.65rem 1.25rem; border-radius: 6px; cursor: pointer; }
    .btn-inspect { background: #ffffff; border: 1px solid #0084cc; color: #0084cc; padding: 0.35rem 0.75rem; border-radius: 4px; font-weight: 600; cursor: pointer; }
    .claims-table { width: 100%; border-collapse: collapse; margin-top: 1rem; }
    .claims-table th { background: #f8fafc; padding: 0.75rem; font-size: 0.75rem; text-align: left; text-transform: uppercase; color: #64748b; border-bottom: 1px solid #e2e8f0; }
    .claims-table td { padding: 0.85rem; font-size: 0.85rem; border-bottom: 1px solid #e2e8f0; }
    .bold-cell { font-weight: 600; color: #002b49; }
    .type-tag { background: #f1f5f9; padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.75rem; font-weight: 600; }
    .empty-state { text-align: center; padding: 2rem; color: #64748b; }
  `]
})
export class ClaimantComponent {
  @Input() claims: Claim[] = [];
  @Output() claimSubmitted = new EventEmitter<any>();
  @Output() selectClaim = new EventEmitter<number>();

  subTab: 'list' | 'new' = 'list';

  fnol = {
    policyNumber: '',
    claimType: 'MOTOR',
    market: 'SINGAPORE',
    incidentDate: new Date().toISOString().split('T')[0],
    incidentDescription: '',
    estimatedAmount: 3200.00
  };

  submitFNOL() {
    if (!this.fnol.policyNumber || !this.fnol.incidentDescription) {
      alert('Policy number and incident description are required.');
      return;
    }
    this.claimSubmitted.emit(this.fnol);
    this.subTab = 'list';
    this.fnol.policyNumber = '';
    this.fnol.incidentDescription = '';
  }
}
