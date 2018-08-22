# Grpc Server
https://github.com/vijay172/grpc-server.git

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
Run from within the distribution with a ./bin/grpc-java-course1 to start the server easily

#Docker

Go to root dir

docker build

docker images | grep grpc

//run docker image exposing 50051 to host
docker run -p 50051:50051 grpc-server
