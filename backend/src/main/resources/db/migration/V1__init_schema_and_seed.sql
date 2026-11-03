-- Chubb APAC Claims Management Platform
-- Initial Schema & Seed Data (6 APAC Markets)

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    market VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Claims Table
CREATE TABLE IF NOT EXISTS claims (
    id BIGSERIAL PRIMARY KEY,
    claim_number VARCHAR(50) NOT NULL UNIQUE,
    policy_number VARCHAR(50) NOT NULL,
    claim_type VARCHAR(30) NOT NULL,
    market VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    incident_date DATE NOT NULL,
    incident_description TEXT NOT NULL,
    estimated_amount NUMERIC(15, 2) NOT NULL,
    approved_amount NUMERIC(15, 2),
    claimant_id BIGINT NOT NULL REFERENCES users(id),
    assigned_officer_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Claim History Table (Audit Trail)
CREATE TABLE IF NOT EXISTS claim_history (
    id BIGSERIAL PRIMARY KEY,
    claim_id BIGINT NOT NULL REFERENCES claims(id),
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_by VARCHAR(100) NOT NULL,
    notes TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Claim Information Requests Table
CREATE TABLE IF NOT EXISTS claim_info_requests (
    id BIGSERIAL PRIMARY KEY,
    claim_id BIGINT NOT NULL REFERENCES claims(id),
    requested_info TEXT NOT NULL,
    requested_by VARCHAR(100) NOT NULL,
    response TEXT,
    responded_at TIMESTAMP,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Claim Documents Table
CREATE TABLE IF NOT EXISTS claim_documents (
    id BIGSERIAL PRIMARY KEY,
    claim_id BIGINT NOT NULL REFERENCES claims(id),
    file_name VARCHAR(255) NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    file_url VARCHAR(500),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- SEED DATA
-- Default password for all demo accounts: 'password123'
-- In BCrypt: $2a$10$wO8I9p6.g4JdJ1E5CgXUf.sM7b8hHl49oR0Wc5n6y/g3.W4G/X.Kq

INSERT INTO users (id, name, email, password, role, market) VALUES
(1, 'Tan Wei Ling', 'claimant.sg@gmail.com', '$2a$10$wO8I9p6.g4JdJ1E5CgXUf.sM7b8hHl49oR0Wc5n6y/g3.W4G/X.Kq', 'CLAIMANT', 'SINGAPORE'),
(2, 'Sarah Jenkins', 'officer@chubb.com', '$2a$10$wO8I9p6.g4JdJ1E5CgXUf.sM7b8hHl49oR0Wc5n6y/g3.W4G/X.Kq', 'CLAIMS_OFFICER', 'SINGAPORE'),
(3, 'Marcus Vance', 'manager@chubb.com', '$2a$10$wO8I9p6.g4JdJ1E5CgXUf.sM7b8hHl49oR0Wc5n6y/g3.W4G/X.Kq', 'CLAIMS_MANAGER', 'SINGAPORE'),
(4, 'David Chen', 'claimant.hk@gmail.com', '$2a$10$wO8I9p6.g4JdJ1E5CgXUf.sM7b8hHl49oR0Wc5n6y/g3.W4G/X.Kq', 'CLAIMANT', 'HONG_KONG'),
(5, 'Liam O''Connor', 'officer.au@chubb.com', '$2a$10$wO8I9p6.g4JdJ1E5CgXUf.sM7b8hHl49oR0Wc5n6y/g3.W4G/X.Kq', 'CLAIMS_OFFICER', 'AUSTRALIA')
ON CONFLICT (id) DO NOTHING;

-- Seed Sample Claims across Markets
INSERT INTO claims (id, claim_number, policy_number, claim_type, market, status, incident_date, incidentDescription, estimated_amount, approved_amount, claimant_id, assigned_officer_id) VALUES
(1, 'CLM-SG-2026-001', 'POL-SG-MOT-9821', 'MOTOR', 'SINGAPORE', 'SUBMITTED', '2026-10-24', 'Rear collision at traffic light intersection on Orchard Rd.', 4500.00, NULL, 1, NULL),
(2, 'CLM-HK-2026-002', 'POL-HK-PRP-4412', 'PROPERTY', 'HONG_KONG', 'UNDER_REVIEW', '2026-10-18', 'Water leakage from upper floor ceiling causing hardwood damage.', 12500.00, NULL, 4, 2),
(3, 'CLM-AU-2026-003', 'POL-AU-MOT-1029', 'MOTOR', 'AUSTRALIA', 'APPROVED', '2026-10-10', 'Hailstorm damage to vehicle windshield and body panels.', 7800.00, 7200.00, 1, 5),
(4, 'CLM-TH-2026-004', 'POL-TH-PRP-8742', 'PROPERTY', 'THAILAND', 'SETTLED', '2026-09-30', 'Factory warehouse minor roof fire damage.', 35000.00, 32000.00, 4, 2)
ON CONFLICT (id) DO NOTHING;
