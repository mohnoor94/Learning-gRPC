package guru.noor.grpc01.calculator.client;

import com.proto.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class CalculatorClient {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50052)
                .usePlaintext()
                .build();
        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);

        SumRequest sumRequest = SumRequest.newBuilder()
                .setFirstNum(10)
                .setSecondNum(25)
                .build();

        SumResponse response = stub.sum(sumRequest);

        System.out.println("Sum is: " + response.getResult());

        // Streaming Server
        stub.primeNumberDecomposition(
                PrimeNumberDecompositionRequest.newBuilder().setNumber(567890000).build()
        ).forEachRemaining(r -> System.out.println(r.getPrimeFactor()));

        channel.shutdown();
    }
}
