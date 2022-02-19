package guru.noor.grpc01.calculator.server;

import com.proto.calculator.*;

import io.grpc.stub.StreamObserver;

public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {

    @Override
    public void sum(SumRequest request, StreamObserver<SumResponse> responseObserver) {
        SumResponse response = SumResponse.newBuilder()
                .setResult(request.getFirstNum() + request.getSecondNum())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void primeNumberDecomposition(PrimeNumberDecompositionRequest request, StreamObserver<PrimeNumberDecompositionResponse> responseObserver) {
        int divisor = 2;
        int number = request.getNumber();
        while (number > 1) {
            if (number % divisor == 0) {
                responseObserver.onNext(PrimeNumberDecompositionResponse.newBuilder().setPrimeFactor(divisor).build());
                number /= divisor;
            } else ++divisor;
        }
        responseObserver.onCompleted();
    }
}
