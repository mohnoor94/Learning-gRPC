package guru.noor.grpc01.calculator.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.io.IOException;

public class CalculatorServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Hello gRPC");

        Server server = ServerBuilder.forPort(50052)
                .addService(new CalculatorServiceImpl())
                .addService(ProtoReflectionService.newInstance()) // reflection
                // use Evans CLI
                // https://github.com/ktr0731/evans
                .build();

        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("SHUTDOWN started!!!");
            server.shutdown();
            System.out.println("SHUTDOWN done!!!");
        }));

        server.awaitTermination();
    }
}
