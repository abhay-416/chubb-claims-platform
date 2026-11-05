import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardMetrics, LiabilityExposure } from '../../core/models/claim.model';

@Component({
  selector: 'app-manager-view',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="manager-container">
      <div class="title-block">
        <h2>Executive Dashboard: Real-time Liability Exposure Across 6 APAC Markets</h2>
        <p class="subtitle">
          Statutory jurisdiction reserves accounting: Motor & Property claims are calculated in native regional currencies.
        </p>
      </div>

      <!-- KPI Summary Cards -->
      <div class="kpi-grid">
        <div class="kpi-card highlight">
          <span class="label">Total Open Active Claims</span>
          <span class="val">{{ metrics?.openClaims || 0 }}</span>
          <span class="sub">Requiring assessment / payout</span>
        </div>
        <div class="kpi-card">
          <span class="label">Settled Payouts</span>
          <span class="val">{{ metrics?.settledClaims || 0 }}</span>
          <span class="sub">Direct financial disbursement completed</span>
        </div>
        <div class="kpi-card">
          <span class="label">Rejected Cases</span>
          <span class="val">{{ metrics?.rejectedClaims || 0 }}</span>
          <span class="sub">Ineligible policy conditions</span>
        </div>
        <div class="kpi-card">
          <span class="label">Total Regional Claims</span>
          <span class="val">{{ metrics?.totalClaims || 0 }}</span>
          <span class="sub">Across all 6 APAC jurisdictions</span>
        </div>
      </div>

      <!-- Statutory Currency Liability Exposure by Jurisdiction -->
      <div class="card market-section">
        <h3>Outstanding Liability Exposure by Market Jurisdiction</h3>
        <p class="section-desc">{{ liability?.note }}</p>

        <div class="market-grid">
          <div class="market-card" *ngFor="let item of liability?.marketLiabilities">
            <div class="market-top">
              <span class="country-name">{{ item.market }}</span>
              <span class="curr-badge">{{ item.currency }}</span>
            </div>
            <div class="exposure-amount">
              {{ item.currencySymbol }} {{ item.outstandingAmount | number:'1.2-2' }}
            </div>
            <div class="market-meta">
              <span>{{ item.openClaimsCount }} open claims pending</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .manager-container { display: flex; flex-direction: column; gap: 1.5rem; }
    .title-block h2 { font-size: 1.35rem; color: #002b49; }
    .subtitle { color: #64748b; font-size: 0.85rem; margin-top: 0.25rem; }
    .kpi-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1.25rem; }
    .kpi-card { background: #ffffff; border: 1px solid #e2e8f0; border-radius: 8px; padding: 1.25rem; display: flex; flex-direction: column; }
    .kpi-card.highlight { border-left: 5px solid #0084cc; background: linear-gradient(135deg, #ffffff 0%, #f0f9ff 100%); }
    .label { font-size: 0.75rem; font-weight: 700; text-transform: uppercase; color: #64748b; margin-bottom: 0.35rem; }
    .val { font-size: 1.8rem; font-weight: 700; color: #002b49; }
    .sub { font-size: 0.75rem; color: #64748b; margin-top: 0.25rem; }
    .card { background: #ffffff; border: 1px solid #e2e8f0; border-radius: 8px; padding: 1.5rem; }
    .section-desc { font-size: 0.8rem; color: #64748b; margin-bottom: 1.25rem; }
    .market-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; }
    .market-card { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; padding: 1rem; }
    .market-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; }
    .country-name { font-size: 0.8rem; font-weight: 700; color: #002b49; }
    .curr-badge { background: #e0f2fe; color: #0369a1; font-size: 0.7rem; font-weight: 700; padding: 0.15rem 0.4rem; border-radius: 4px; }
    .exposure-amount { font-size: 1.4rem; font-weight: 700; color: #002b49; }
    .market-meta { font-size: 0.75rem; color: #64748b; margin-top: 0.35rem; }
  `]
})
export class ManagerComponent {
  @Input() metrics: DashboardMetrics | null = null;
  @Input() liability: LiabilityExposure | null = null;
}
