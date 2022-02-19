package guru.noor.grpc01.greeting.client;

import com.proto.greet.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Iterator;

public class GreetingClient {

    public static void main(String[] args) {
        System.out.println("Hello, gRPC Client!");

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext() // don't do this in production - disabling SSL.
                .build();

        System.out.println("Creating Stub");
        // DummyServiceGrpc.DummyServiceBlockingStub syncClient = DummyServiceGrpc.newBlockingStub(channel);
        // DummyServiceGrpc.DummyServiceFutureStub asyncClient = DummyServiceGrpc.newFutureStub(channel);
        // syncClient. doSomething() ...

        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);
        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Ali")
                .setLastName("AlAhmad")
                .build();

        // Unary
        /*
        GreetRequest greetRequest = GreetRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        GreetResponse greetResponse = greetClient.greet(greetRequest);

        System.out.println(greetResponse.getResult());
         */

        GreetManyTimesRequest greetManyTimesRequest = GreetManyTimesRequest.newBuilder().setGreeting(greeting).build();
        Iterator<GreetManyTimesResponse> greetManyTimesResponseIterator = greetClient.greetManyTimes(greetManyTimesRequest);

        greetManyTimesResponseIterator.forEachRemaining(response -> System.out.println(response.getResult()));

        System.out.println("Shutting down channel");
        channel.shutdown();
    }
}
