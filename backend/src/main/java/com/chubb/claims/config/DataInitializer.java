package com.chubb.claims.config;

import com.chubb.claims.domain.*;
import com.chubb.claims.infrastructure.ClaimHistoryRepository;
import com.chubb.claims.infrastructure.ClaimRepository;
import com.chubb.claims.infrastructure.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ClaimRepository claimRepository;
    private final ClaimHistoryRepository historyRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ClaimRepository claimRepository,
                           ClaimHistoryRepository historyRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.claimRepository = claimRepository;
        this.historyRepository = historyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Seeding initial demo users for 6 APAC markets...");
            String encodedPassword = passwordEncoder.encode("password123");

            User claimantSg = userRepository.save(new User("Tan Wei Ling", "claimant.sg@gmail.com", encodedPassword, UserRole.CLAIMANT, Market.SINGAPORE));
            User officerSg = userRepository.save(new User("Sarah Jenkins", "officer@chubb.com", encodedPassword, UserRole.CLAIMS_OFFICER, Market.SINGAPORE));
            User managerSg = userRepository.save(new User("Marcus Vance", "manager@chubb.com", encodedPassword, UserRole.CLAIMS_MANAGER, Market.SINGAPORE));
            User claimantHk = userRepository.save(new User("David Chen", "claimant.hk@gmail.com", encodedPassword, UserRole.CLAIMANT, Market.HONG_KONG));
            User officerAu = userRepository.save(new User("Liam O'Connor", "officer.au@chubb.com", encodedPassword, UserRole.CLAIMS_OFFICER, Market.AUSTRALIA));

            log.info("Seeding realistic sample claims for demonstration...");
            // Claim 1: SUBMITTED (Motor, Singapore)
            Claim claim1 = new Claim();
            claim1.setClaimNumber("CLM-SG-2026-1001");
            claim1.setPolicyNumber("POL-SG-MOT-9821");
            claim1.setClaimType(ClaimType.MOTOR);
            claim1.setMarket(Market.SINGAPORE);
            claim1.setStatus(ClaimStatus.SUBMITTED);
            claim1.setIncidentDate(LocalDate.now().minusDays(3));
            claim1.setIncidentDescription("Rear collision at traffic intersection along Orchard Road during heavy rain.");
            claim1.setEstimatedAmount(new BigDecimal("4500.00"));
            claim1.setClaimant(claimantSg);
            claimRepository.save(claim1);
            historyRepository.save(new ClaimHistory(claim1, null, ClaimStatus.SUBMITTED, claimantSg.getEmail(), "First Notice of Loss submitted."));

            // Claim 2: UNDER_REVIEW (Property, Hong Kong)
            Claim claim2 = new Claim();
            claim2.setClaimNumber("CLM-HK-2026-1002");
            claim2.setPolicyNumber("POL-HK-PRP-4412");
            claim2.setClaimType(ClaimType.PROPERTY);
            claim2.setMarket(Market.HONG_KONG);
            claim2.setStatus(ClaimStatus.UNDER_REVIEW);
            claim2.setIncidentDate(LocalDate.now().minusDays(10));
            claim2.setIncidentDescription("Major water pipe burst on upper floor causing ceiling collapse and hardwood flooding.");
            claim2.setEstimatedAmount(new BigDecimal("18500.00"));
            claim2.setClaimant(claimantHk);
            claim2.setAssignedOfficer(officerSg);
            claimRepository.save(claim2);
            historyRepository.save(new ClaimHistory(claim2, ClaimStatus.SUBMITTED, ClaimStatus.ASSIGNED, officerSg.getEmail(), "Assigned to Sarah Jenkins."));
            historyRepository.save(new ClaimHistory(claim2, ClaimStatus.ASSIGNED, ClaimStatus.UNDER_REVIEW, officerSg.getEmail(), "On-site loss adjuster dispatched."));

            // Claim 3: APPROVED (Motor, Australia)
            Claim claim3 = new Claim();
            claim3.setClaimNumber("CLM-AU-2026-1003");
            claim3.setPolicyNumber("POL-AU-MOT-1029");
            claim3.setClaimType(ClaimType.MOTOR);
            claim3.setMarket(Market.AUSTRALIA);
            claim3.setStatus(ClaimStatus.APPROVED);
            claim3.setIncidentDate(LocalDate.now().minusDays(15));
            claim3.setIncidentDescription("Severe hailstorm shattered windscreen and dented bonnet and roof.");
            claim3.setEstimatedAmount(new BigDecimal("8200.00"));
            claim3.setApprovedAmount(new BigDecimal("7600.00"));
            claim3.setClaimant(claimantSg);
            claim3.setAssignedOfficer(officerAu);
            claimRepository.save(claim3);
            historyRepository.save(new ClaimHistory(claim3, ClaimStatus.UNDER_REVIEW, ClaimStatus.APPROVED, officerAu.getEmail(), "Workshop quote approved. Excess deducted."));

            // Claim 4: SETTLED (Property, Thailand)
            Claim claim4 = new Claim();
            claim4.setClaimNumber("CLM-TH-2026-1004");
            claim4.setPolicyNumber("POL-TH-PRP-8742");
            claim4.setClaimType(ClaimType.PROPERTY);
            claim4.setMarket(Market.THAILAND);
            claim4.setStatus(ClaimStatus.SETTLED);
            claim4.setIncidentDate(LocalDate.now().minusDays(30));
            claim4.setIncidentDescription("Warehouse electrical short-circuit localized fire.");
            claim4.setEstimatedAmount(new BigDecimal("42000.00"));
            claim4.setApprovedAmount(new BigDecimal("39500.00"));
            claim4.setClaimant(claimantHk);
            claim4.setAssignedOfficer(officerSg);
            claimRepository.save(claim4);
            historyRepository.save(new ClaimHistory(claim4, ClaimStatus.APPROVED, ClaimStatus.SETTLED, officerSg.getEmail(), "Direct bank disbursement completed."));

            log.info("Demo data initialized successfully.");
        }
    }
}
