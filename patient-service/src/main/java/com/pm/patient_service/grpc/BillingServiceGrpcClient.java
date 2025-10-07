package com.pm.patient_service.grpc;

// Import the main gRPC service class
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.pm.billingservice.grpc.BillingRequest;
import com.pm.billingservice.grpc.BillingResponse;
import com.pm.billingservice.grpc.BillingServiceGrpc;
import com.pm.billingservice.grpc.PatientDeletionRequest;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

@Service
public class BillingServiceGrpcClient {

        private final Logger log = LoggerFactory.getLogger(BillingServiceGrpcClient.class);

        public final BillingServiceGrpc.BillingServiceBlockingStub blockingStub;

        public BillingServiceGrpcClient(@Value("${billing.service.address:localhost}") String serverAddress,
                        @Value("${billing.service.grpc.port:9091}") int serverPort) {

                log.info("Connecting to Billing Service using GRPC at --> ServerAddress: {}, ServerPort: {}",
                                serverAddress,
                                serverPort);

                ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress, serverPort)
                                .usePlaintext()
                                .build();

                blockingStub = BillingServiceGrpc.newBlockingStub(channel);
        }

        public BillingResponse createBillingAccount(String patientId, String name, String email) {
                BillingRequest request = BillingRequest.newBuilder()
                                .setPatientId(patientId)
                                .setName(name)
                                .setEmail(email)
                                .build();

                BillingResponse response = blockingStub.createBillingAccount(request);
                return response;
        }

        public void processPatientDeletion(String patientId) {
                PatientDeletionRequest request = PatientDeletionRequest.newBuilder()
                                .setPatientId(patientId)
                                .build();
                log.info("Sending gRPC notification of patient deletion for patientId: {}", patientId);
                blockingStub.processPatientDeletion(request);
        }
}
