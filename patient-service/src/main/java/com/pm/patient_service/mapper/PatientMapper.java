package com.pm.patient_service.mapper;

import java.time.LocalDate;

import com.pm.patient_service.dto.PatientRequestDTO;
import com.pm.patient_service.dto.PatientResponseDTO;
import com.pm.patient_service.model.Patient;

public class PatientMapper {

    public static PatientResponseDTO toDTO(Patient patient) {
        PatientResponseDTO patientdto = new PatientResponseDTO();
        patientdto.setId(patient.getId().toString());
        patientdto.setName(patient.getName());
        patientdto.setEmail(patient.getEmail());
        patientdto.setAddress(patient.getAddress());
        patientdto.setDateofbirth(patient.getDateofbirth().toString());
        return patientdto;
    }

    public static Patient toModel(PatientRequestDTO patientdto) {
        Patient patient = new Patient();
        patient.setName(patientdto.getName());
        patient.setEmail(patientdto.getEmail());
        patient.setAddress(patientdto.getAddress());
        patient.setDateofbirth(LocalDate.parse(patientdto.getDateofbirth()));
        patient.setRegisteredDate(LocalDate.parse(patientdto.getRegisteredDate()));
        return patient;
    }

}