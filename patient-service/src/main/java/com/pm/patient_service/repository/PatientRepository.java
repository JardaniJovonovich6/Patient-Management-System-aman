package com.pm.patient_service.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pm.patient_service.model.Patient;
// import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, UUID> {
    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);
}
