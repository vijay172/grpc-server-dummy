package com.intel.grpc.image.client;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Uninterruptibles;
import com.proto.image.CopyImageRequest;
import com.proto.image.CopyImageResponse;
import com.proto.image.ImageServiceGrpc;
import com.proto.image.ReadImageRequest;
import com.proto.image.ReadImageResponse;
import com.proto.image.Roi;
import io.grpc.Deadline;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ImageClient {
    private ManagedChannel channel;
    public ImageClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext() //avoid ssl issues
                .build();
    }

    public static void main(String[] args) {
        System.out.println("Hello, I am a gRPC client");
        //input arguments
        String inputFile = args[0];//"test.txt";
        String fileLocation = args[1];//"tmp";
        String options = args[2];//"test";
        Roi roi = Roi.newBuilder()
                .setX(1)
                .setY(2)
                .setWidth(3)
                .setHeight(4)
                .build();
        long deadlineDuration = Long.parseLong(args[3]);//200 ms
        String host = args[4];//"localhost";
        int port = Integer.parseInt(args[5]);//50051;

        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext() //avoid ssl issues
                .build();
        System.out.println("Creating Image service client stub");
        //DummyServiceGrpc.DummyServiceBlockingStub syncClient = DummyServiceGrpc.newBlockingStub(channel);
        //DummyServiceGrpc.DummyServiceFutureStub asyncClient = DummyServiceGrpc.newFutureStub(channel);//TODO:
        //final ImageServiceGrpc.ImageServiceStub asyncClient = ImageServiceGrpc.newStub(channel);


        try {
            //works
            //copyImageFutureAsync(deadlineDuration, inputFile, fileLocation, channel);
            //unary stream doesn't work
            //copyImageReqStreamAsync(deadlineDuration, inputFile, fileLocation, channel);
            //bidi async works
            //copyImageBidiAsync(deadlineDuration, inputFile, fileLocation, channel);
            //synchronous call
            for (int i = 0; i < 4; i++) {
                System.out.println("copyImageSync Create request with count:" + i);
                options = UUID.randomUUID().toString();
                //makes a connection each time ???
                copyImageSync(deadlineDuration, inputFile + i, fileLocation + i, roi, options, channel);
            }
            //TODO: how to wait for response before calling channel shutdown
            //copyImageAsync(asyncClient, deadlineDuration,latch, inputFile, fileLocation, options,channel);
        } finally {
            //TODO: channel shutdown
            System.out.println("Shutting down Channel");
            //channel.shutdown();
        }
    }

    public static void copyImageFutureAsync(long deadlineDuration,
                                             String inputFile, String fileLocation,
                                             ManagedChannel channel) {
        System.out.println("Entered copyImageFutureAsync()");
        CountDownLatch latch = new CountDownLatch(1);//TODO: unbounded ???
        try {
            //asynchronous Future client
            final ImageServiceGrpc.ImageServiceFutureStub asyncImageClient = ImageServiceGrpc.newFutureStub(channel);
            String options = UUID.randomUUID().toString();
            //set up protobuf copy image request
            CopyImageRequest copyImageRequest = CopyImageRequest.newBuilder()
                    .setInputFile(inputFile)
                    .setFileLocation(fileLocation)
                    .setOptions(options)
                    .build();
            //async call
            final ListenableFuture<CopyImageResponse> copyImageAsyncResponse =
                    asyncImageClient
                            .withDeadline(Deadline.after(deadlineDuration, TimeUnit.SECONDS)) //change to Milliseconds to simulate deadline exception
                            .copyImage(copyImageRequest);
            //Ugly API compared to node.js async handling
            Futures.addCallback(copyImageAsyncResponse,
                    new FutureCallback<CopyImageResponse>() {
                        @Override
                        public void onSuccess(@Nullable final CopyImageResponse copyImageResponse) {
                            assert copyImageResponse != null;
                            System.out.println("copyImageResponse:" + copyImageResponse.getResult());
                            latch.countDown();
                            System.out.println("Shutting down channel");
                            channel.shutdown();
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            Status status = Status.fromThrowable(throwable);
                            if (status.getCode() == Status.Code.DEADLINE_EXCEEDED) {
                                System.out.println("Deadline has been exceeded, we don't want the response");
                            } else {
                                throwable.printStackTrace();
                            }
                            latch.countDown();
                            System.out.println("Shutting down channel");
                            channel.shutdown();
                        }
                    }, MoreExecutors.directExecutor());

            //TODO: shud i put in finally
            if (!Uninterruptibles.awaitUninterruptibly(latch, 1, TimeUnit.SECONDS)) {
                throw new RuntimeException("copyImageFutureAsync - latch timeout!");
            }
        /*
        ReadImageRequest readImageRequest = ReadImageRequest.newBuilder()
                .setFileLocation(fileLocation)
                .setRoi(roi)
                .setOptions(options)
                .build();

        ReadImageResponse readImageResponse = imageClient //.withDeadline(Deadline.after(deadlineDuration, TimeUnit.MILLISECONDS))
                .readImage(readImageRequest);
            System.out.println("readImageResponse:" + readImageResponse.getResult());*/
        } catch (StatusRuntimeException e) { //TODO:StatusRuntimeException
            if (e.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
                System.out.println("Deadline has been exceeded, we don't want the response");
            } else {
                e.printStackTrace();
            }
        }
    }

    public static void copyImageReqStreamAsync(long deadlineDuration,
                                                String inputFile, String fileLocation,
                                                ManagedChannel channel) {
        System.out.println("Entered copyImageReqStreamAsync()");
        int count = 4;
        CountDownLatch latch = new CountDownLatch(count);//TODO: unbounded ???
        Map<String, ArrayList<String>> requestMap = new HashMap<>();
        try {
            //asynchronous client
            final ImageServiceGrpc.ImageServiceStub asyncImageClient = ImageServiceGrpc.newStub(channel);
            String options = "";
            final StreamObserver<CopyImageRequest> requestCopyObserver = asyncImageClient
                    .withDeadline(Deadline.after(deadlineDuration, TimeUnit.SECONDS)) //change to Milliseconds to simulate deadline exception
                    .copyImageReqStream(new StreamObserver<CopyImageResponse>() {
                        @Override
                        public void onNext(CopyImageResponse value) {
                            System.out.println("Response from server:" + value.getResult());
                            //TODO: match requestId from options with requestMap

                        }

                        @Override
                        public void onError(Throwable t) {
                            latch.countDown();
                            System.err.println("copyImageReqStreamAsync Error thrown:" + t);//TODO: maybe retry ?
                            if (t instanceof StatusRuntimeException) {
                                StatusRuntimeException e = (StatusRuntimeException) t;
                                if (e.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
                                    System.out.println("copyImageReqStreamAsync Deadline has been exceeded, we don't want the response");
                                } else {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onCompleted() {
                            System.out.println("copyImageReqStreamAsync Server is done sending data");
                            latch.countDown();
                        }
                    });
            //setup request
            for (int i = 0; i < count; i++) {
                System.out.println("copyImageReqStreamAsync Create request with count:" + i);
                options = UUID.randomUUID().toString();
                ArrayList<String> dataLst = new ArrayList<>();
                dataLst.add(inputFile + i);
                dataLst.add(fileLocation + i);
                dataLst.add(options);
                requestMap.put(options, dataLst);
                requestCopyObserver.onNext(
                        CopyImageRequest.newBuilder()
                                .setInputFile(inputFile + i)
                                .setFileLocation(fileLocation + i)
                                .setOptions(options)
                                .build());
            }
            //done with request - else keep it open
            requestCopyObserver.onCompleted();

            //TODO: shud i put in finally
            if (!Uninterruptibles.awaitUninterruptibly(latch, 500, TimeUnit.SECONDS)) { //TODO: Latch timeout as parm
                throw new RuntimeException("copyImageReqStreamAsync - latch timeout!");
            }
        /*
        ReadImageRequest readImageRequest = ReadImageRequest.newBuilder()
                .setFileLocation(fileLocation)
                .setRoi(roi)
                .setOptions(options)
                .build();

        ReadImageResponse readImageResponse = imageClient //.withDeadline(Deadline.after(deadlineDuration, TimeUnit.MILLISECONDS))
                .readImage(readImageRequest);
            System.out.println("readImageResponse:" + readImageResponse.getResult());*/
        } catch (StatusRuntimeException e) { //TODO:StatusRuntimeException
            if (e.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
                System.out.println("Deadline has been exceeded, we don't want the response");
            } else {
                e.printStackTrace();
            }
        } finally {
            //TODO: maybe close channel here
            //channel.shutdown();
        }
    }


    public static void copyImageBidiAsync(long deadlineDuration,
                                           String inputFile, String fileLocation,
                                           ManagedChannel channel) {
        int count = 4;
        CountDownLatch latch = new CountDownLatch(count);//TODO: unbounded ???
        Map<String, ArrayList<String>> requestMap = new HashMap<>();
        final ImageServiceGrpc.ImageServiceStub asyncImageClient = ImageServiceGrpc.newStub(channel);
        try {
            String options = "";
            //how to handle copyImage response
            final StreamObserver<CopyImageRequest> requestCopyObserver = asyncImageClient
                    .withDeadline(Deadline.after(deadlineDuration, TimeUnit.SECONDS)) //change to Milliseconds to simulate deadline exception
                    .copyImageBidi(new StreamObserver<CopyImageResponse>() {
                        @Override
                        public void onNext(CopyImageResponse value) {
                            System.out.println("copyImageBidiAsync Response from server:" + value.getResult());
                            //TODO: match with requestMap for options as key
                        }

                        @Override
                        public void onError(Throwable t) {
                            latch.countDown();
                            System.err.println("copyImageBidiAsync Error thrown:" + t);//TODO: maybe retry ?
                            if (t instanceof StatusRuntimeException) {
                                StatusRuntimeException e = (StatusRuntimeException) t;
                                if (e.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
                                    System.out.println("copyImageBidiAsync Deadline has been exceeded, we don't want the response");
                                } else {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onCompleted() {
                            System.out.println("copyImageBidiAsync Server is done sending data");
                            latch.countDown();
                        }
                    });

            //setup request
            for (int i = 0; i < count; i++) {
                System.out.println("copyImageBidiAsync Create request with count:" + i);
                options = UUID.randomUUID().toString();
                ArrayList<String> dataLst = new ArrayList<>();
                dataLst.add(inputFile + i);
                dataLst.add(fileLocation + i);
                dataLst.add(options);
                requestMap.put(options, dataLst);
                requestCopyObserver.onNext(
                        CopyImageRequest.newBuilder()
                                .setInputFile(inputFile + i)
                                .setFileLocation(fileLocation + i)
                                .setOptions(options)
                                .build());
            }

            requestCopyObserver.onCompleted();

            try {
                latch.await(500, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                System.err.println("copyImageBidiAsync Latch Interrupt exception:" + e);
                e.printStackTrace();
            }
        } finally {
            System.out.println("copyImageBidiAsync Shutting down channel");
            channel.shutdown();
        }
    }


    public static void copyImageSync(long deadlineDuration,
                                       String inputFile, String fileLocation, Roi roi, String options,
                                      ManagedChannel channel) {
        try {
            //blocking synchronous client
            final ImageServiceGrpc.ImageServiceBlockingStub imageClient = ImageServiceGrpc.newBlockingStub(channel);
            //set up protobuf copy image request
            CopyImageRequest copyImageRequest = CopyImageRequest.newBuilder()
                    .setInputFile(inputFile)
                    .setFileLocation(fileLocation)
                    .setOptions(options)
                    .build();
            CopyImageResponse copyImageResponse = imageClient.withDeadline(Deadline.after(deadlineDuration, TimeUnit.SECONDS))
                    .copyImage(copyImageRequest);
            System.out.println("copyImageResponse:" + copyImageResponse.getResult());

            ReadImageRequest readImageRequest = ReadImageRequest.newBuilder()
                    .setFileLocation(fileLocation)
                    .setRoi(roi)
                    .setOptions(options)
                    .build();

            ReadImageResponse readImageResponse = imageClient.withDeadline(Deadline.after(deadlineDuration, TimeUnit.SECONDS))
                    .readImage(readImageRequest);
            System.out.println("readImageResponse:" + readImageResponse.getResult());
        } catch (StatusRuntimeException e) { //TODO:StatusRuntimeException
            if (e.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
                System.out.println("Deadline has been exceeded, we don't want the response");
            } else {
                e.printStackTrace();
            }
        }
    }
}
