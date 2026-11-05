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
  currency: string;
  status: 'SUBMITTED' | 'ASSIGNED' | 'UNDER_REVIEW' | 'ADDITIONAL_INFO_REQUIRED' | 'APPROVED' | 'REJECTED' | 'SETTLED';
  incidentDate: string;
  incidentDescription: string;
  estimatedAmount: number;
  approvedAmount?: number;
  claimantId?: number;
  claimantName?: string;
  assignedOfficerId?: number;
  assignedOfficerName?: string;
  version: number;
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

export interface MarketLiability {
  market: string;
  currency: string;
  currencySymbol: string;
  outstandingAmount: number;
  openClaimsCount: number;
}

export interface LiabilityExposure {
  note: string;
  marketLiabilities: MarketLiability[];
  typeExposureByCurrency: Record<string, Record<string, number>>;
}
