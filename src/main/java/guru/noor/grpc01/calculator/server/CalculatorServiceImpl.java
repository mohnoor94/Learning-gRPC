package guru.noor.grpc01.calculator.server;

import com.proto.calculator.*;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public StreamObserver<ComputeAverageRequest> computeAverage(StreamObserver<ComputeAverageResponse> responseObserver) {
        return new StreamObserver<ComputeAverageRequest>() {
            private final List<Integer> numbers = new ArrayList<>();

            @Override
            public void onNext(ComputeAverageRequest value) {
                numbers.add(value.getNumber());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                if (numbers.isEmpty()) numbers.add(0);

                @SuppressWarnings("OptionalGetWithoutIsPresent")
                double average = numbers.stream().mapToInt(i -> i).average().getAsDouble();

                responseObserver.onNext(
                        ComputeAverageResponse.newBuilder().setAverage(average).build()
                );
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<FindMaximumRequest> findMaximum(StreamObserver<FindMaximumResponse> responseObserver) {
        return new StreamObserver<>() {
            private int maxSoFar = Integer.MIN_VALUE;

            @Override
            public void onNext(FindMaximumRequest value) {
                if (value.getNumber() > maxSoFar) {
                    maxSoFar = value.getNumber();
                    responseObserver.onNext(
                            FindMaximumResponse.newBuilder().setMax(maxSoFar).build()
                    );
                }
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onCompleted();
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(FindMaximumResponse.newBuilder().setMax(maxSoFar).build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void squareRoot(SquareRootRequest request, StreamObserver<SquareRootResponse> responseObserver) {
        int number = request.getNumber();

        if (number < 0) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("Negative Number")
                            .augmentDescription("Number sent: " + number)
                            .asRuntimeException()
            );
            return;
        }

        responseObserver.onNext(SquareRootResponse.newBuilder().setRoot(Math.sqrt(number)).build());
        responseObserver.onCompleted();
    }
}
