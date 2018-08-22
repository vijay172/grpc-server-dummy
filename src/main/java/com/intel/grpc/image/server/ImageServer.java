package com.intel.grpc.image.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class ImageServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Hello gRPC Image server");

        Server server = ServerBuilder.forPort(50051)
                .addService(new ImageServiceImpl())
                .build();

        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread( () -> {
            System.out.println("Received Shutdown request");
            server.shutdown();
            System.out.println("Successfully stopped the server");
        }));

        server.awaitTermination();
    }
}
