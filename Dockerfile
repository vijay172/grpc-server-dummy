FROM openjdk:8
WORKDIR ./
ADD build $HOME/build
RUN unzip build/distributions/grpc-java-course1-1.0-SNAPSHOT.zip -d "/app/"

#COPY /app/grpc-java-course1-1.0-SNAPSHOT/bin /app/bin
#COPY /app/grpc-java-course1-1.0-SNAPSHOT/lib /app/lib

EXPOSE 50051
#ENTRYPOINT ["java", "-jar", "/app/lib/grpc-java-course1-1.0-SNAPSHOT.jar"]
#ENTRYPOINT ["java", "-jar", "/app/grpc-java-course1-1.0-SNAPSHOT/lib/grpc-java-course1-1.0-SNAPSHOT.jar"]
ENTRYPOINT ["/app/grpc-java-course1-1.0-SNAPSHOT/bin/grpc-java-course1"]


