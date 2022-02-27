package guru.noor.grpc01.greeting.client;

import com.proto.greet.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {

    public static void main(String[] args) {
        // System.out.println("Creating Stub");
        // DummyServiceGrpc.DummyServiceBlockingStub syncClient = DummyServiceGrpc.newBlockingStub(channel);
        // DummyServiceGrpc.DummyServiceFutureStub asyncClient = DummyServiceGrpc.newFutureStub(channel);
        // syncClient. doSomething() ...

        new GreetingClient().run();
    }

    public void run() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext() // don't do this in production - disabling SSL.
                .build();

        //doUnaryCall(channel);
        //doServerStreamingCall(channel);
        doClientStreamingCall(channel);

        System.out.println("Shutting down channel");
        channel.shutdown();
    }

    private void doUnaryCall(ManagedChannel channel) {
        System.out.println("Hello, gRPC Client!");

        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);
        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Ali")
                .setLastName("AlAhmad")
                .build();

        // Unary
        GreetRequest greetRequest = GreetRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        GreetResponse greetResponse = greetClient.greet(greetRequest);

        System.out.println(greetResponse.getResult());
        channel.shutdown();
    }

    private void doServerStreamingCall(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);
        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Ali")
                .setLastName("AlAhmad")
                .build();

        GreetManyTimesRequest greetManyTimesRequest = GreetManyTimesRequest.newBuilder().setGreeting(greeting).build();
        Iterator<GreetManyTimesResponse> greetManyTimesResponseIterator = greetClient.greetManyTimes(greetManyTimesRequest);

        greetManyTimesResponseIterator.forEachRemaining(response -> System.out.println(response.getResult()));
    }

    private void doClientStreamingCall(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<LongGreetRequest> requestObserver = asyncClient.longGreet(new StreamObserver<>() {
            @Override
            public void onNext(LongGreetResponse value) {
                // will be called only once.
                System.out.println("Received:: ");
                System.out.println(value.getResult());
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {
                System.out.println("DONE!");
                latch.countDown();
            }
        });

        requestObserver.onNext(
                LongGreetRequest.newBuilder().setGreeting(
                        Greeting.newBuilder().setFirstName("Huda").build()
                ).build()
        );

        requestObserver.onNext(
                LongGreetRequest.newBuilder().setGreeting(
                        Greeting.newBuilder().setFirstName("Noor").build()
                ).build()
        );

        requestObserver.onNext(
                LongGreetRequest.newBuilder().setGreeting(
                        Greeting.newBuilder().setFirstName("Nat").build()
                ).build()
        );

        requestObserver.onCompleted();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
