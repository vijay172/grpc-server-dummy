package com.intel.grpc.image.server;

import com.proto.image.CopyImageRequest;
import com.proto.image.CopyImageResponse;
import com.proto.image.ImageServiceGrpc;
import com.proto.image.ReadImageRequest;
import com.proto.image.ReadImageResponse;
import com.proto.image.Roi;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

class ImageServiceImpl extends ImageServiceGrpc.ImageServiceImplBase {

    @Override
    public StreamObserver<CopyImageRequest> copyImageReqStream(StreamObserver<CopyImageResponse> responseObserver) {
        System.out.println("Entered copyImageReqStream()");

        return new StreamObserver<CopyImageRequest>() {
            @Override
            public void onNext(CopyImageRequest request) {
                String inputFile = request.getInputFile();
                String fileLocation = request.getFileLocation();
                String options = request.getOptions();

                String result = String.format("CopyImage inputFile:%s, fileLocation:%s, options:%s", inputFile, fileLocation, options);
                CopyImageResponse response = CopyImageResponse.newBuilder()
                        .setResult(result)
                        .build();
                //send the response
                responseObserver.onNext(response);
                //responseObserver.onCompleted();
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(
                        Status.INTERNAL
                                .withDescription("copyImageReqStream Internal Error")
                                .augmentDescription("Error details:" + t.getMessage())
                                .asRuntimeException()
                );
            }

            @Override
            public void onCompleted() {
                //complete rpc call
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<ReadImageRequest> readImageReqStream(StreamObserver<ReadImageResponse> responseObserver) {
        System.out.println("Entered readImageReqStream()");
        return new StreamObserver<ReadImageRequest>() {
            @Override
            public void onNext(ReadImageRequest request) {
                String fileLocation = request.getFileLocation();
                Roi roi = request.getRoi();
                String options = request.getOptions();

                String result = String.format("ReadImageReqStream fileLocation:%s, roi:%s, options:%s", fileLocation, roi, options);
                ReadImageResponse response = ReadImageResponse.newBuilder()
                        .setResult(result)
                        .build();
                responseObserver.onNext(response);
                //responseObserver.onCompleted();
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(
                        Status.INTERNAL
                                .withDescription("readImageReqStream Internal Error")
                                .augmentDescription("Error details:") //TODO: exception details
                                .asRuntimeException()
                );
            }

            @Override
            public void onCompleted() {
                //responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<CopyImageRequest> copyImageBidi(StreamObserver<CopyImageResponse> responseObserver) {
        System.out.println("Entered copyImageBidi()");

        return new StreamObserver<CopyImageRequest>() {
            @Override
            public void onNext(CopyImageRequest request) {
                String inputFile = request.getInputFile();
                String fileLocation = request.getFileLocation();
                String options = request.getOptions();

                String result = String.format("copyImageBidi inputFile:%s, fileLocation:%s, options:%s", inputFile, fileLocation, options);
                CopyImageResponse response = CopyImageResponse.newBuilder()
                        .setResult(result)
                        .build();
                //send the response
                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(
                        Status.INTERNAL
                                .withDescription("copyImageBidi Internal Error")
                                .augmentDescription("Error details:")
                                .asRuntimeException()
                );
            }

            @Override
            public void onCompleted() {
                //complete rpc call
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<ReadImageRequest> readImageBidi(StreamObserver<ReadImageResponse> responseObserver) {
        System.out.println("Entered readImageBidi()");
        return new StreamObserver<ReadImageRequest>() {
            @Override
            public void onNext(ReadImageRequest request) {
                String fileLocation = request.getFileLocation();
                Roi roi = request.getRoi();
                String options = request.getOptions();

                String result = String.format("ReadImage fileLocation:%s, roi:%s, options:%s", fileLocation, roi, options);
                ReadImageResponse response = ReadImageResponse.newBuilder()
                        .setResult(result)
                        .build();
                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(
                        Status.INTERNAL
                                .withDescription("readImage Internal Error")
                                .augmentDescription("Error details:") //TODO: exception details
                                .asRuntimeException()
                );
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    public void copyImage(CopyImageRequest request, StreamObserver<CopyImageResponse> responseObserver) {
        System.out.println("Entered copyImage()");
        String inputFile = request.getInputFile();
        String fileLocation = request.getFileLocation();
        String options = request.getOptions();

        String result = String.format("CopyImage inputFile:%s, fileLocation:%s, options:%s", inputFile, fileLocation, options);
        CopyImageResponse response = CopyImageResponse.newBuilder()
                .setResult(result)
                .build();
        /*try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {}*/
        //send the response
        responseObserver.onNext(response);
        //complete rpc call
        responseObserver.onCompleted();
    }

    public void readImage(ReadImageRequest request, StreamObserver<ReadImageResponse> responseObserver) {
        System.out.println("Entered readImage()");
        String fileLocation = request.getFileLocation();
        Roi roi = request.getRoi();
        String options = request.getOptions();

        String result = String.format("ReadImage fileLocation:%s, roi:%s, options:%s", fileLocation, roi, options);
        ReadImageResponse response = ReadImageResponse.newBuilder()
                .setResult(result)
                .build();
        if (response != null) {
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Internal Error")
                            .augmentDescription("Error details:")
                            .asRuntimeException()
            );
        }

    }
}
