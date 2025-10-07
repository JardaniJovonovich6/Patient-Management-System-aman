package com.pm.billing_service.entity;

// import jakarta.annotation.Generated;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class BillingAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID Id;

    private String patientId;
    private String name;
    private String email;
    private String status;

    public UUID getId() {
        return this.Id;
    }

    public void setId(UUID Id) {
        this.Id = Id;
    }

    public String getPatientId() {
        return this.patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
