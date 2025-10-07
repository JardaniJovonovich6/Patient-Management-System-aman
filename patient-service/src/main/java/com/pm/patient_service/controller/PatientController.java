package com.pm.patient_service.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pm.patient_service.dto.PatientRequestDTO;
import com.pm.patient_service.dto.PatientResponseDTO;
import com.pm.patient_service.dto.validators.CreatePatientValidationGroup;
import com.pm.patient_service.service.PatientService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;

@RestController
@RequestMapping("/patients")
@Tag(name = "Patient Management", description = "APIs for managing patients")
public class PatientController {

    private final PatientService patientservice;

    public PatientController(PatientService patientservice) {
        this.patientservice = patientservice;
    }

    @GetMapping
    @Operation(summary = "Gets all patients", description = "Retrieves a list of all patients in the system")
    public ResponseEntity<List<PatientResponseDTO>> getallpatients() {
        List<PatientResponseDTO> patients = patientservice.getallpatients();
        return ResponseEntity.ok().body(patients);
    }

    @PostMapping
    @Operation(summary = "Create patients", description = "Creates a new patient in the system")
    public ResponseEntity<PatientResponseDTO> savePatient(@Validated({ Default.class,
            CreatePatientValidationGroup.class }) @RequestBody PatientRequestDTO patientrequestdto) {
        PatientResponseDTO patientresponsedto = patientservice.savePatient(patientrequestdto);
        return ResponseEntity.ok().body(patientresponsedto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "update patients", description = "Update patients in the system")
    public ResponseEntity<PatientResponseDTO> updatePatient(@PathVariable UUID id,
            @Validated({ Default.class }) @RequestBody PatientRequestDTO patientrequestdto) {
        PatientResponseDTO patientResponseDTO = patientservice.updatePatient(id, patientrequestdto);
        return ResponseEntity.ok().body(patientResponseDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete patient", description = "Delete a patient in the system")
    public ResponseEntity<String> deletePatient(@PathVariable UUID id) {
        patientservice.deletePatient(id);
        return ResponseEntity.ok().body("Patient deleted sucessfully : " + id);
    }

}
