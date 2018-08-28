# Grpc Server
https://github.com/vijay172/grpc-server.git
This project is used to build a target jar file based on a proto file in src/proto
Target built jar is available in build/libs or you can use build/distributions/*.zip or *.tar
This generated jar can be used in the gRPC client to call the gRPC remote service.

# Content

- Image Service
- Unary, Server Streaming, Client Streaming, BiDi Streaming
- Error Handling, Deadlines, SSL Encryption

# Build
Go to root directory
./gradlew build

# How to run
In root dir:
java -jar build/libs/grpc-java-course1-1.0-SNAPSHOT.jar

OR

./gradlew run

If successful, you should see on the console:
Hello gRPC Image server

#Distribution
./gradlew distZip

Generates a zip file inside build/distributions/

This zip or tar file contains a bin directory.
Run from within the unzipped or untarred distribution with a ./bin/grpc-java-course1 to start the server easily

#Docker
You can also use a Docker image to run this Dummy gRPC server to test your client code.

Go to root dir

docker build

docker images | grep grpc

//run docker image exposing port 50051 from within host container(first port specified) to docker container at 
port 50051(2nd port specified)

docker run -p 50051:50051 grpc-server

#Building code
Protobuf interface resides in src/main/java/proto/image/image.proto

Main code is inside src/main/java/com/intel/grpc/image/server directory

2 main classes are 
1. ImageServer to start the gRPC server.
2. ImageServiceImpl to implement the Interface specified in image.proto

An example client implementation to test the gRPC server code is in client/ImageClient

