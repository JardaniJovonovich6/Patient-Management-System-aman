package com.pm.billing_service.grpc;

// Import the correct, generated gRPC classes
import com.pm.billingservice.grpc.BillingRequest;
import com.pm.billingservice.grpc.BillingResponse;
import com.pm.billingservice.grpc.PatientDeletionRequest; // Correct request class
import com.pm.billingservice.grpc.BillingServiceGrpc.BillingServiceImplBase;

// Imports for database classes
import com.pm.billing_service.entity.BillingAccount;
import com.pm.billing_service.repo.BillingAccountRepository;

// Standard library imports
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@GrpcService
public class BillingGrpcService extends BillingServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(BillingGrpcService.class);
    private final BillingAccountRepository repository;

    @Autowired
    public BillingGrpcService(BillingAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public void createBillingAccount(BillingRequest request, StreamObserver<BillingResponse> responseObserver) {
        BillingAccount newAccount = new BillingAccount();
        newAccount.setPatientId(request.getPatientId());
        newAccount.setName(request.getName());
        newAccount.setEmail(request.getEmail());
        newAccount.setStatus("ACTIVE");

        BillingAccount savedAccount = repository.save(newAccount);

        BillingResponse response = BillingResponse.newBuilder()
                .setAccountId(savedAccount.getId().toString())
                .setStatus(savedAccount.getStatus())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void processPatientDeletion(PatientDeletionRequest request,
            StreamObserver<BillingResponse> responseObserver) {
        log.info("gRPC request to process deletion for patientId: {}", request.getPatientId());

        repository.findByPatientId(request.getPatientId()).ifPresent(account -> {
            account.setStatus("INACTIVE");
            repository.save(account);

            BillingResponse response = BillingResponse.newBuilder()
                    .setAccountId(account.getId().toString())
                    .setStatus(account.getStatus())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        });
    }
}