package guru.noor.grpc01.greeting.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class GreetingServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Hello gRPC");

        Server server = ServerBuilder.forPort(50051).build();
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("SHUTDOWN started!!!");
            server.shutdown();
            System.out.println("SHUTDOWN done!!!");
        }));

        server.awaitTermination();
    }
}
