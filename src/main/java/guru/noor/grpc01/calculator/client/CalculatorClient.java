package guru.noor.grpc01.calculator.client;

import com.proto.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class CalculatorClient {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        //doUnaryCall(channel);
        //doStreamingServerCall(channel);
        //doStreamingClientCall(channel);
        doBiDiStreamingCal(channel);

        channel.shutdown();
    }

    private static void doUnaryCall(ManagedChannel channel) {
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
            public void onError(Throwable t) {
            }

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

    private static void doBiDiStreamingCal(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceStub asyncClient = CalculatorServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<FindMaximumRequest> requestObserver = asyncClient.findMaximum(new StreamObserver<FindMaximumResponse>() {
            @Override
            public void onNext(FindMaximumResponse value) {
                System.out.println("Response from Sever:: " + value.getMax());
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server is DONE!!!");
                latch.countDown();
            }
        });

        Arrays.asList(1, 5, 3, 6, 2, 20, 9, 5, 7, 300, 10, 500).forEach(number -> {
            System.out.println("Sending:: " + number);
            requestObserver.onNext(FindMaximumRequest.newBuilder().setNumber(number).build());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        requestObserver.onCompleted();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
