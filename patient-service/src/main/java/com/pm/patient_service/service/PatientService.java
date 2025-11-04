package com.pm.patient_service.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.pm.patient_service.dto.PatientRequestDTO;
import com.pm.patient_service.dto.PatientResponseDTO;
import com.pm.patient_service.exception.EmailAlreadyExistsException;
import com.pm.patient_service.exception.PatientNotFoundException;
import com.pm.patient_service.grpc.BillingServiceGrpcClient;
import com.pm.patient_service.kafka.KafkaProducer; // Import KafkaProducer
import com.pm.patient_service.mapper.PatientMapper;
import com.pm.patient_service.model.Patient;
import com.pm.patient_service.repository.PatientRepository;

@Service
public class PatientService {

    private static final Logger log = LoggerFactory.getLogger(PatientService.class);
    private final PatientRepository patientrepository;
    private final BillingServiceGrpcClient billingservicegrpcclient;
    private final KafkaProducer kafkaProducer; // Add KafkaProducer field

    // Update the constructor to accept KafkaProducer
    public PatientService(PatientRepository patientrepository, BillingServiceGrpcClient billingservicegrpcclient,
            KafkaProducer kafkaProducer) {
        this.patientrepository = patientrepository;
        this.billingservicegrpcclient = billingservicegrpcclient;
        this.kafkaProducer = kafkaProducer; // Initialize KafkaProducer
    }

    // Read
    public List<PatientResponseDTO> getallpatients() {
        List<Patient> patientlist = patientrepository.findAll();
        List<PatientResponseDTO> patientdtolist = patientlist.stream()
                .map(patientlistitem -> PatientMapper.toDTO(patientlistitem)).toList();
        return patientdtolist;
    }

    // Create
    public PatientResponseDTO savePatient(PatientRequestDTO patientrequestdto) {
        if (patientrepository.existsByEmail(patientrequestdto.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Email already exists in the system for a patient : " + patientrequestdto.getEmail());
        }

        Patient newpatient = patientrepository.save(PatientMapper.toModel(patientrequestdto));
        log.info("Patient saved to database with ID: {}", newpatient.getId());

        // Temporarily disabled for our isolated Kafka test
        billingservicegrpcclient.createBillingAccount(newpatient.getId().toString(),
        newpatient.getName(), newpatient.getEmail());
        log.info("gRPC call to billing-service SKIPPED.");

        // Call the Kafka Producer
        kafkaProducer.sendEvent(newpatient);
        log.info("Kafka sendEvent method has been called.");

        return PatientMapper.toDTO(newpatient);
    }

    // Update
    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientrequestdto) {
        Patient patient = patientrepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with id : {} " + id));
        patient.setName(patientrequestdto.getName());
        patient.setEmail(patientrequestdto.getEmail());
        patient.setAddress(patientrequestdto.getAddress());
        patient.setDateofbirth(LocalDate.parse(patientrequestdto.getDateofbirth()));
        if (patientrepository.existsByEmailAndIdNot(patientrequestdto.getEmail(), id)) {
            throw new EmailAlreadyExistsException(
                    "Email already exists in the system for a patient : " + patientrequestdto.getEmail());
        }
        Patient updatedPatient = patientrepository.save(patient);
        return PatientMapper.toDTO(updatedPatient);
    }

    // delete
    public String deletePatient(UUID id) {
        Patient patient = patientrepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with id : {} " + id));
        patientrepository.delete(patient);
        billingservicegrpcclient.processPatientDeletion(id.toString());
        patientrepository.deleteById(id);
        return "Patient deleted successfully with id : " + id;
    }
}