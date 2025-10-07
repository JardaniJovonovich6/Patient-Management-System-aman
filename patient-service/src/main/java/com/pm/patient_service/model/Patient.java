package com.pm.patient_service.model;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

@Entity
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    private String name;

    @NotNull
    @Email
    @Column(unique = true)
    private String email;

    @NotNull
    private String address;

    @NotNull
    private LocalDate dateofbirth;

    @NotNull
    private LocalDate registeredDate;

    public Patient(UUID id, String name, String email, String address, LocalDate dateofbirth, LocalDate registeredDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.address = address;
        this.dateofbirth = dateofbirth;
        this.registeredDate = registeredDate;
    }

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getDateofbirth() {
        return this.dateofbirth;
    }

    public void setDateofbirth(LocalDate dateofbirth) {
        this.dateofbirth = dateofbirth;
    }

    public LocalDate getRegisteredDate() {
        return this.registeredDate;
    }

    public void setRegisteredDate(LocalDate registeredDate) {
        this.registeredDate = registeredDate;
    }

    @Override
    public String toString() {
        return "{" +
                " id='" + getId() + "'" +
                ", name='" + getName() + "'" +
                ", email='" + getEmail() + "'" +
                ", address='" + getAddress() + "'" +
                ", dateofbirth='" + getDateofbirth() + "'" +
                ", registeredDate='" + getRegisteredDate() + "'" +
                "}";
    }

    public Patient() {
    }

}
