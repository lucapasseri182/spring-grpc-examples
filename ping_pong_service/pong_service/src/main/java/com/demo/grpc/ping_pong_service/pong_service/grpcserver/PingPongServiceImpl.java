package com.demo.grpc.ping_pong_service.pong_service.grpcserver;

import com.demo.grpc.ping_pong_service.PingPongServiceGrpc;
import com.demo.grpc.ping_pong_service.PingRequest;
import com.demo.grpc.ping_pong_service.PongResponse;
import com.demo.grpc.ping_pong_service.pong_service.service.PongService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

@GrpcService
public class PingPongServiceImpl extends PingPongServiceGrpc.PingPongServiceImplBase {

    private final PongService pongService;

    @Autowired
    public PingPongServiceImpl(PongService pongService) {
        this.pongService = pongService;
    }

    @Override
    public void pingPong(PingRequest request, StreamObserver<PongResponse> responseObserver) {
        String pong = pongService.pingPong(request.getPing());
        PongResponse response = PongResponse.newBuilder()
                .setPong(pong)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}