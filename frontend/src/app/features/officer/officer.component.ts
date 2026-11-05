import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Claim, ClaimDetail } from '../../core/models/claim.model';

@Component({
  selector: 'app-officer-view',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="officer-container">
      <div class="header-controls">
        <h3>Chubb Incoming Claims Workload Queue</h3>
        <div class="filters">
          <select [(ngModel)]="filterMarket" (change)="onFilterChange()">
            <option value="">All 6 APAC Markets</option>
            <option value="SINGAPORE">Singapore (SGD)</option>
            <option value="HONG_KONG">Hong Kong (HKD)</option>
            <option value="AUSTRALIA">Australia (AUD)</option>
            <option value="THAILAND">Thailand (THB)</option>
            <option value="MALAYSIA">Malaysia (MYR)</option>
            <option value="INDONESIA">Indonesia (IDR)</option>
          </select>
          <select [(ngModel)]="filterStatus" (change)="onFilterChange()">
            <option value="">All Statuses</option>
            <option value="SUBMITTED">Submitted</option>
            <option value="ASSIGNED">Assigned</option>
            <option value="UNDER_REVIEW">Under Review</option>
            <option value="ADDITIONAL_INFO_REQUIRED">Info Required</option>
            <option value="APPROVED">Approved</option>
            <option value="REJECTED">Rejected</option>
            <option value="SETTLED">Settled</option>
          </select>
        </div>
      </div>

      <div class="card table-card">
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
              <th>Assigned Officer</th>
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
              <td>{{ c.assignedOfficerName || 'Unassigned' }}</td>
              <td>
                <button class="btn-inspect" (click)="selectClaim.emit(c.id)">Review & Assess</button>
              </td>
            </tr>
            <tr *ngIf="claims.length === 0">
              <td colspan="9" class="empty-state">No claims found matching current filter parameters.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .officer-container { display: flex; flex-direction: column; gap: 1rem; }
    .header-controls { display: flex; justify-content: space-between; align-items: center; }
    .filters { display: flex; gap: 0.75rem; }
    select { padding: 0.5rem 0.75rem; border: 1px solid #e2e8f0; border-radius: 6px; font-size: 0.85rem; }
    .card { background: #ffffff; border: 1px solid #e2e8f0; border-radius: 8px; padding: 1.5rem; }
    .claims-table { width: 100%; border-collapse: collapse; }
    .claims-table th { background: #f8fafc; padding: 0.75rem; font-size: 0.75rem; text-align: left; text-transform: uppercase; color: #64748b; border-bottom: 1px solid #e2e8f0; }
    .claims-table td { padding: 0.85rem; font-size: 0.85rem; border-bottom: 1px solid #e2e8f0; }
    .bold-cell { font-weight: 600; color: #002b49; }
    .type-tag { background: #f1f5f9; padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.75rem; font-weight: 600; }
    .btn-inspect { background: #ffffff; border: 1px solid #0084cc; color: #0084cc; padding: 0.35rem 0.75rem; border-radius: 4px; font-weight: 600; cursor: pointer; }
    .empty-state { text-align: center; padding: 2rem; color: #64748b; }
  `]
})
export class OfficerComponent {
  @Input() claims: Claim[] = [];
  @Output() selectClaim = new EventEmitter<number>();
  @Output() filterChanged = new EventEmitter<{ market: string; status: string }>();

  filterMarket: string = '';
  filterStatus: string = '';

  onFilterChange() {
    this.filterChanged.emit({ market: this.filterMarket, status: this.filterStatus });
  }
}
