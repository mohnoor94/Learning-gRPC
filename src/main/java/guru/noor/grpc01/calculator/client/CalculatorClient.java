package guru.noor.grpc01.calculator.client;

import com.proto.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;

public class CalculatorClient {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        //doUnaryCall(channel);
        //doStreamingServerCall(channel);
        doStreamingClientCall(channel);

        channel.shutdown();
    }

    private static void doUnaryCall(ManagedChannel channel){
        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);

        SumRequest sumRequest = SumRequest.newBuilder()
                .setFirstNum(10)
                .setSecondNum(25)
                .build();

        SumResponse response = stub.sum(sumRequest);

        System.out.println("Sum is: " + response.getResult());
    }

    private static void doStreamingServerCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);

        stub.primeNumberDecomposition(
                PrimeNumberDecompositionRequest.newBuilder().setNumber(567890000).build()
        ).forEachRemaining(r -> System.out.println(r.getPrimeFactor()));
    }

    private static void doStreamingClientCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceStub stub = CalculatorServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<ComputeAverageRequest> computeAverageRequestStreamObserver = stub.computeAverage(new StreamObserver<>() {
            @Override
            public void onNext(ComputeAverageResponse value) {
                // will be called only once.
                System.out.println("Average:: ");
                System.out.println(value.getAverage());
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {
                System.out.println("DONE");
                latch.countDown();
            }
        });

        computeAverageRequestStreamObserver.onNext(ComputeAverageRequest.newBuilder().setNumber(1).build());
        computeAverageRequestStreamObserver.onNext(ComputeAverageRequest.newBuilder().setNumber(2).build());
        computeAverageRequestStreamObserver.onNext(ComputeAverageRequest.newBuilder().setNumber(3).build());
        computeAverageRequestStreamObserver.onNext(ComputeAverageRequest.newBuilder().setNumber(4).build());

        computeAverageRequestStreamObserver.onCompleted();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
