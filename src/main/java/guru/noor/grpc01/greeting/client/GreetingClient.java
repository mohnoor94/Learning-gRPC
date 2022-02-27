package guru.noor.grpc01.greeting.client;

import com.proto.greet.*;
import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {

    public static void main(String[] args) throws SSLException {
        // System.out.println("Creating Stub");
        // DummyServiceGrpc.DummyServiceBlockingStub syncClient = DummyServiceGrpc.newBlockingStub(channel);
        // DummyServiceGrpc.DummyServiceFutureStub asyncClient = DummyServiceGrpc.newFutureStub(channel);
        // syncClient. doSomething() ...

        new GreetingClient().run();
    }

    public void run() throws SSLException {
        // Unsafe channel
//        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
//                .usePlaintext() // don't do this in production - disabling SSL.
//                .build();

        // Safe channel
        ManagedChannel channel = NettyChannelBuilder.forAddress("localhost", 50051)
                .sslContext(GrpcSslContexts.forClient().trustManager(new File("ssl/ca.crt")).build())
                .build();

        //doUnaryCall(channel);
        //doServerStreamingCall(channel);
        //doClientStreamingCall(channel);
        //doBiDiStreamingCall(channel);

        doUnaryCallWithDeadline(channel);

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
            public void onError(Throwable t) {
            }

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

    private void doBiDiStreamingCall(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<GreetEveryoneRequest> requestObserver = asyncClient.greetEveryone(new StreamObserver<>() {

            @Override
            public void onNext(GreetEveryoneResponse value) {
                System.out.println("Response from server:: " + value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server is DONE!!");
                latch.countDown();
            }
        });

        List<String> names = Arrays.asList("Noor", "Nat", "Aseel");
        names.forEach(name -> {
            System.out.println("Sending:: " + name);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            requestObserver.onNext(GreetEveryoneRequest.newBuilder().setGreeting(
                    Greeting.newBuilder().setFirstName(name).build()).build()
            );
        });

        requestObserver.onCompleted();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doUnaryCallWithDeadline(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceBlockingStub blockingStub = GreetServiceGrpc.newBlockingStub(channel);

        doOneCallWithDeadline(blockingStub, 3000); // First call: 3000 ms deadline - will succeed
        doOneCallWithDeadline(blockingStub, 250); // First call: 100 ms deadline - will fail
    }

    private void doOneCallWithDeadline(GreetServiceGrpc.GreetServiceBlockingStub blockingStub, int deadline) {
        try {
            System.out.println("Sending a request with a deadline of " + deadline + "ms.");
            GreetWithDeadlineResponse response = blockingStub.withDeadline(Deadline.after(deadline, TimeUnit.MILLISECONDS)).greetWithDeadline(
                    GreetWithDeadlineRequest.newBuilder().setGreeting(
                            Greeting.newBuilder().setFirstName("Noor").build()
                    ).build()
            );
            System.out.println("Result:: " + response.getResult());
        } catch (StatusRuntimeException e) {
            if (e.getStatus() == Status.DEADLINE_EXCEEDED) {
                System.out.println("Deadline exceeded!");
            } else {
                e.printStackTrace();
            }
        }
    }
}
