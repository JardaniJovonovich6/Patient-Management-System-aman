package com.pm.billing_service.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pm.billing_service.entity.BillingAccount;

public interface BillingAccountRepository extends JpaRepository<BillingAccount, UUID> {
    Optional<BillingAccount> findByPatientId(String patientId);
}
