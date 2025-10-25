package com.pm.patient_service.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.pm.patient_service.model.Patient;

import patient.event.PatientEvent;

@Service
public class KafkaProducer {
    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(Patient patient) {
        PatientEvent event = PatientEvent.newBuilder().setPatientId(patient.getId().toString())
                .setName(patient.getName()).setEmail(patient.getEmail()).setEventType("PATIENT_CREATED").build();

        try {
            kafkaTemplate.send("patient", event.toByteArray());
        } catch (Exception e) {
            log.error("Error in sending patient event -- type Patient_created : {}", event);
        }

    }
}
