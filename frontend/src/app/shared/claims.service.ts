import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

export interface User {
  id: number;
  name: string;
  email: string;
  role: 'CLAIMANT' | 'CLAIMS_OFFICER' | 'CLAIMS_MANAGER';
  market: string;
}

export interface Claim {
  id: number;
  claimNumber: string;
  policyNumber: string;
  claimType: 'MOTOR' | 'PROPERTY';
  market: string;
  status: 'SUBMITTED' | 'ASSIGNED' | 'UNDER_REVIEW' | 'ADDITIONAL_INFO_REQUIRED' | 'APPROVED' | 'REJECTED' | 'SETTLED';
  incidentDate: string;
  incidentDescription: string;
  estimatedAmount: number;
  approvedAmount?: number;
  claimantId?: number;
  claimantName?: string;
  assignedOfficerId?: number;
  assignedOfficerName?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ClaimDetail {
  claim: Claim;
  history: Array<{
    id: number;
    oldStatus: string;
    newStatus: string;
    changedBy: string;
    notes: string;
    timestamp: string;
  }>;
  infoRequests: Array<{
    id: number;
    requestedInfo: string;
    requestedBy: string;
    response?: string;
    respondedAt?: string;
    status: string;
    createdAt: string;
  }>;
}

export interface DashboardMetrics {
  totalClaims: number;
  openClaims: number;
  settledClaims: number;
  rejectedClaims: number;
  statusBreakdown: Record<string, number>;
  marketBreakdown: Record<string, number>;
}

export interface LiabilityExposure {
  totalOutstandingLiability: number;
  marketExposure: Record<string, number>;
  typeExposure: Record<string, number>;
}

@Injectable({
  providedIn: 'root'
})
export class ClaimsService {
  private apiUrl = 'http://localhost:8080/api/v1';
  private currentToken: string = '';

  constructor(private http: HttpClient) {}

  setToken(token: string) {
    this.currentToken = token;
  }

  private getHeaders(): HttpHeaders {
    let headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    if (this.currentToken) {
      headers = headers.set('Authorization', `Bearer ${this.currentToken}`);
    }
    return headers;
  }

  login(email: string, password: string = 'password123'): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/login`, { email, password });
  }

  getClaims(status?: string, market?: string): Observable<Claim[]> {
    let url = `${this.apiUrl}/claims`;
    const params: string[] = [];
    if (status) params.push(`status=${status}`);
    if (market) params.push(`market=${market}`);
    if (params.length > 0) url += `?${params.join('&')}`;

    return this.http.get<Claim[]>(url, { headers: this.getHeaders() }).pipe(
      catchError(() => of(this.getMockClaims()))
    );
  }

  getClaimDetail(id: number): Observable<ClaimDetail> {
    return this.http.get<ClaimDetail>(`${this.apiUrl}/claims/${id}`, { headers: this.getHeaders() }).pipe(
      catchError(() => of(this.getMockClaimDetail(id)))
    );
  }

  submitClaim(payload: any): Observable<Claim> {
    return this.http.post<Claim>(`${this.apiUrl}/claims`, payload, { headers: this.getHeaders() });
  }

  assignClaim(id: number, officerId?: number): Observable<Claim> {
    return this.http.post<Claim>(`${this.apiUrl}/claims/${id}/assign`, officerId || null, { headers: this.getHeaders() });
  }

  startReview(id: number): Observable<Claim> {
    return this.http.post<Claim>(`${this.apiUrl}/claims/${id}/review`, {}, { headers: this.getHeaders() });
  }

  requestInfo(id: number, requestedInfo: string): Observable<Claim> {
    return this.http.post<Claim>(`${this.apiUrl}/claims/${id}/request-info`, { requestedInfo }, { headers: this.getHeaders() });
  }

  submitInfo(id: number, response: string): Observable<Claim> {
    return this.http.post<Claim>(`${this.apiUrl}/claims/${id}/submit-info`, { response }, { headers: this.getHeaders() });
  }

  assessClaim(id: number, decision: 'APPROVE' | 'REJECT', approvedAmount?: number, notes?: string): Observable<Claim> {
    return this.http.post<Claim>(`${this.apiUrl}/claims/${id}/assess`, { decision, approvedAmount, notes }, { headers: this.getHeaders() });
  }

  settleClaim(id: number, settlementNotes?: string): Observable<Claim> {
    return this.http.post<Claim>(`${this.apiUrl}/claims/${id}/settle`, settlementNotes || '', { headers: this.getHeaders() });
  }

  getMetrics(): Observable<DashboardMetrics> {
    return this.http.get<DashboardMetrics>(`${this.apiUrl}/dashboard/metrics`, { headers: this.getHeaders() }).pipe(
      catchError(() => of(this.getMockMetrics()))
    );
  }

  getLiabilityExposure(): Observable<LiabilityExposure> {
    return this.http.get<LiabilityExposure>(`${this.apiUrl}/dashboard/liability`, { headers: this.getHeaders() }).pipe(
      catchError(() => of(this.getMockLiability()))
    );
  }

  // Fallback demo data if backend is offline during frontend-only test
  private getMockClaims(): Claim[] {
    return [
      {
        id: 1,
        claimNumber: 'CLM-SG-2026-1001',
        policyNumber: 'POL-SG-MOT-9821',
        claimType: 'MOTOR',
        market: 'SINGAPORE',
        status: 'SUBMITTED',
        incidentDate: '2026-10-24',
        incidentDescription: 'Rear collision at traffic light intersection on Orchard Rd.',
        estimatedAmount: 4500.00,
        claimantId: 1,
        claimantName: 'Tan Wei Ling',
        createdAt: '2026-10-24T09:30:00',
        updatedAt: '2026-10-24T09:30:00'
      },
      {
        id: 2,
        claimNumber: 'CLM-HK-2026-1002',
        policyNumber: 'POL-HK-PRP-4412',
        claimType: 'PROPERTY',
        market: 'HONG_KONG',
        status: 'UNDER_REVIEW',
        incidentDate: '2026-10-18',
        incidentDescription: 'Water pipe burst causing substantial ceiling and floor damage.',
        estimatedAmount: 18500.00,
        claimantId: 4,
        claimantName: 'David Chen',
        assignedOfficerId: 2,
        assignedOfficerName: 'Sarah Jenkins',
        createdAt: '2026-10-18T14:15:00',
        updatedAt: '2026-10-19T10:00:00'
      },
      {
        id: 3,
        claimNumber: 'CLM-AU-2026-1003',
        policyNumber: 'POL-AU-MOT-1029',
        claimType: 'MOTOR',
        market: 'AUSTRALIA',
        status: 'APPROVED',
        incidentDate: '2026-10-10',
        incidentDescription: 'Hailstorm damage to body panels and windscreen.',
        estimatedAmount: 8200.00,
        approvedAmount: 7600.00,
        claimantId: 1,
        claimantName: 'Tan Wei Ling',
        assignedOfficerId: 5,
        assignedOfficerName: 'Liam O\'Connor',
        createdAt: '2026-10-10T11:20:00',
        updatedAt: '2026-10-14T16:45:00'
      }
    ];
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
      totalOutstandingLiability: 30600.00,
      marketExposure: {
        'SINGAPORE': 4500.00,
        'HONG_KONG': 18500.00,
        'AUSTRALIA': 7600.00,
        'THAILAND': 0,
        'MALAYSIA': 0,
        'INDONESIA': 0
      },
      typeExposure: {
        'MOTOR': 12100.00,
        'PROPERTY': 18500.00
      }
    };
  }
}
