import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Claim, ClaimDetail, DashboardMetrics, LiabilityExposure, User } from '../../core/models/claim.model';

@Injectable({
  providedIn: 'root'
})
export class ClaimsService {
  private apiUrl = 'http://localhost:8080/api/v1';
  private currentToken: string = '';

  // Explicit Demo Mode Toggle (Never silently fallback!)
  public isDemoMode: boolean = false;

  constructor(private http: HttpClient) {}

  setToken(token: string) {
    this.currentToken = token;
  }

  setDemoMode(enabled: boolean) {
    this.isDemoMode = enabled;
  }

  private getHeaders(): HttpHeaders {
    let headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    if (this.currentToken) {
      headers = headers.set('Authorization', `Bearer ${this.currentToken}`);
    }
    return headers;
  }

  login(email: string, password: string = 'password123'): Observable<any> {
    if (this.isDemoMode) {
      return of({
        token: 'demo-jwt-token',
        user: { id: 1, name: 'Tan Wei Ling', email, role: 'CLAIMANT', market: 'SINGAPORE' }
      });
    }
    return this.http.post(`${this.apiUrl}/auth/login`, { email, password });
  }

  getClaims(status?: string, market?: string): Observable<Claim[]> {
    if (this.isDemoMode) {
      return of(this.getMockClaims(status, market));
    }

    let url = `${this.apiUrl}/claims`;
    const params: string[] = [];
    if (status) params.push(`status=${status}`);
    if (market) params.push(`market=${market}`);
    if (params.length > 0) url += `?${params.join('&')}`;

    return this.http.get<Claim[]>(url, { headers: this.getHeaders() });
  }

  getClaimDetail(id: number): Observable<ClaimDetail> {
    if (this.isDemoMode) {
      return of(this.getMockClaimDetail(id));
    }
    return this.http.get<ClaimDetail>(`${this.apiUrl}/claims/${id}`, { headers: this.getHeaders() });
  }

  submitClaim(payload: any): Observable<Claim> {
    if (this.isDemoMode) {
      const mock = {
        id: Math.floor(Math.random() * 1000) + 10,
        claimNumber: `CLM-${payload.market.substring(0,2)}-2026-${Math.floor(1000 + Math.random() * 9000)}`,
        ...payload,
        currency: payload.currency || 'SGD',
        status: 'SUBMITTED',
        version: 0,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      return of(mock);
    }
    return this.http.post<Claim>(`${this.apiUrl}/claims`, payload, { headers: this.getHeaders() });
  }

  assignClaim(id: number, officerId?: number): Observable<Claim> {
    if (this.isDemoMode) {
      return of({ ...this.getMockClaims()[0], status: 'ASSIGNED', assignedOfficerName: 'Sarah Jenkins' });
    }
    return this.http.post<Claim>(`${this.apiUrl}/claims/${id}/assign`, officerId || null, { headers: this.getHeaders() });
  }

  startReview(id: number): Observable<Claim> {
    if (this.isDemoMode) {
      return of({ ...this.getMockClaims()[0], status: 'UNDER_REVIEW' });
    }
    return this.http.post<Claim>(`${this.apiUrl}/claims/${id}/review`, {}, { headers: this.getHeaders() });
  }

  requestInfo(id: number, requestedInfo: string): Observable<Claim> {
    if (this.isDemoMode) {
      return of({ ...this.getMockClaims()[0], status: 'ADDITIONAL_INFO_REQUIRED' });
    }
    return this.http.post<Claim>(`${this.apiUrl}/claims/${id}/request-info`, { requestedInfo }, { headers: this.getHeaders() });
  }

  submitInfo(id: number, response: string): Observable<Claim> {
    if (this.isDemoMode) {
      return of({ ...this.getMockClaims()[0], status: 'UNDER_REVIEW' });
    }
    return this.http.post<Claim>(`${this.apiUrl}/claims/${id}/submit-info`, { response }, { headers: this.getHeaders() });
  }

  assessClaim(id: number, decision: 'APPROVE' | 'REJECT', approvedAmount?: number, notes?: string): Observable<Claim> {
    if (this.isDemoMode) {
      return of({
        ...this.getMockClaims()[0],
        status: decision === 'APPROVE' ? 'APPROVED' : 'REJECTED',
        approvedAmount: decision === 'APPROVE' ? approvedAmount : undefined
      });
    }
    return this.http.post<Claim>(`${this.apiUrl}/claims/${id}/assess`, { decision, approvedAmount, notes }, { headers: this.getHeaders() });
  }

  settleClaim(id: number, settlementNotes?: string): Observable<Claim> {
    if (this.isDemoMode) {
      return of({ ...this.getMockClaims()[0], status: 'SETTLED' });
    }
    return this.http.post<Claim>(`${this.apiUrl}/claims/${id}/settle`, settlementNotes || '', { headers: this.getHeaders() });
  }

  getMetrics(): Observable<DashboardMetrics> {
    if (this.isDemoMode) {
      return of(this.getMockMetrics());
    }
    return this.http.get<DashboardMetrics>(`${this.apiUrl}/dashboard/metrics`, { headers: this.getHeaders() });
  }

  getLiabilityExposure(): Observable<LiabilityExposure> {
    if (this.isDemoMode) {
      return of(this.getMockLiability());
    }
    return this.http.get<LiabilityExposure>(`${this.apiUrl}/dashboard/liability`, { headers: this.getHeaders() });
  }

  // Realistic mock data used ONLY when isDemoMode === true
  private getMockClaims(status?: string, market?: string): Claim[] {
    const list: Claim[] = [
      {
        id: 1,
        claimNumber: 'CLM-SG-2026-1001',
        policyNumber: 'POL-SG-MOT-9821',
        claimType: 'MOTOR',
        market: 'SINGAPORE',
        currency: 'SGD',
        status: 'SUBMITTED',
        incidentDate: '2026-10-24',
        incidentDescription: 'Rear collision at traffic intersection along Orchard Rd.',
        estimatedAmount: 4500.00,
        claimantId: 1,
        claimantName: 'Tan Wei Ling',
        version: 0,
        createdAt: '2026-10-24T09:30:00',
        updatedAt: '2026-10-24T09:30:00'
      },
      {
        id: 2,
        claimNumber: 'CLM-HK-2026-1002',
        policyNumber: 'POL-HK-PRP-4412',
        claimType: 'PROPERTY',
        market: 'HONG_KONG',
        currency: 'HKD',
        status: 'UNDER_REVIEW',
        incidentDate: '2026-10-18',
        incidentDescription: 'Water pipe burst causing damage to ceiling and floors.',
        estimatedAmount: 125000.00,
        claimantId: 4,
        claimantName: 'David Chen',
        assignedOfficerId: 2,
        assignedOfficerName: 'Sarah Jenkins',
        version: 0,
        createdAt: '2026-10-18T14:15:00',
        updatedAt: '2026-10-19T10:00:00'
      },
      {
        id: 3,
        claimNumber: 'CLM-AU-2026-1003',
        policyNumber: 'POL-AU-MOT-1029',
        claimType: 'MOTOR',
        market: 'AUSTRALIA',
        currency: 'AUD',
        status: 'APPROVED',
        incidentDate: '2026-10-10',
        incidentDescription: 'Severe hailstorm damage across car body.',
        estimatedAmount: 8200.00,
        approvedAmount: 7600.00,
        claimantId: 1,
        claimantName: 'Tan Wei Ling',
        assignedOfficerId: 5,
        assignedOfficerName: 'Liam O\'Connor',
        version: 0,
        createdAt: '2026-10-10T11:20:00',
        updatedAt: '2026-10-14T16:45:00'
      }
    ];

    return list.filter(c => (!status || c.status === status) && (!market || c.market === market));
  }

  private getMockClaimDetail(id: number): ClaimDetail {
    const claim = this.getMockClaims().find(c => c.id === id) || this.getMockClaims()[0];
    return {
      claim,
      history: [
        { id: 1, oldStatus: '', newStatus: 'SUBMITTED', changedBy: 'Tan Wei Ling', notes: 'FNOL submitted', timestamp: '2026-10-24T09:30:00' },
        { id: 2, oldStatus: 'SUBMITTED', newStatus: 'ASSIGNED', changedBy: 'Sarah Jenkins', notes: 'Assigned to Sarah Jenkins', timestamp: '2026-10-24T10:00:00' }
      ],
      infoRequests: []
    };
  }

  private getMockMetrics(): DashboardMetrics {
    return {
      totalClaims: 4,
      openClaims: 2,
      settledClaims: 1,
      rejectedClaims: 0,
      statusBreakdown: { 'SUBMITTED': 1, 'UNDER_REVIEW': 1, 'APPROVED': 1, 'SETTLED': 1 },
      marketBreakdown: { 'SINGAPORE': 1, 'HONG_KONG': 1, 'AUSTRALIA': 1, 'THAILAND': 1 }
    };
  }

  private getMockLiability(): LiabilityExposure {
    return {
      note: 'Statutory jurisdiction currency calculations for accurate insurance accounting.',
      marketLiabilities: [
        { market: 'SINGAPORE', currency: 'SGD', currencySymbol: 'S$', outstandingAmount: 4500.00, openClaimsCount: 1 },
        { market: 'HONG_KONG', currency: 'HKD', currencySymbol: 'HK$', outstandingAmount: 125000.00, openClaimsCount: 1 },
        { market: 'AUSTRALIA', currency: 'AUD', currencySymbol: 'A$', outstandingAmount: 7600.00, openClaimsCount: 1 },
        { market: 'THAILAND', currency: 'THB', currencySymbol: '฿', outstandingAmount: 0.00, openClaimsCount: 0 },
        { market: 'MALAYSIA', currency: 'MYR', currencySymbol: 'RM', outstandingAmount: 0.00, openClaimsCount: 0 },
        { market: 'INDONESIA', currency: 'IDR', currencySymbol: 'Rp', outstandingAmount: 0.00, openClaimsCount: 0 }
      ],
      typeExposureByCurrency: {
        'MOTOR': { 'SGD': 4500.00, 'AUD': 7600.00 },
        'PROPERTY': { 'HKD': 125000.00 }
      }
    };
  }
}
