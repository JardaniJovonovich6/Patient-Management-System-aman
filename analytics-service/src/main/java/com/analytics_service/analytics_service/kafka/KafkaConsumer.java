package com.analytics_service.analytics_service.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.google.protobuf.InvalidProtocolBufferException;

import patient.event.PatientEvent;

@Service
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    @KafkaListener(topics = "patient", groupId = "analytics_group")
    // The only change is Byte[] -> byte[]
    public void consumeEvent(ConsumerRecord<String, byte[]> record) {
        try {
            byte[] eventBytes = record.value();

            PatientEvent patientEvent = PatientEvent.parseFrom(eventBytes);

            log.info("SUCCESS: New event consumed and parsed from partition {} with offset {}",
                    record.partition(), record.offset());
            log.info("--> Event details: {}", patientEvent);

        } catch (InvalidProtocolBufferException e) {
            log.error("Error deserializing the event message: {}", e.getMessage());
        }
    }
}