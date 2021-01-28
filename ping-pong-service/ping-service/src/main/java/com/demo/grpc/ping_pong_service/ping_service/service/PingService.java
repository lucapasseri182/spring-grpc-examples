package com.demo.grpc.ping_pong_service.ping_service.service;

import com.demo.grpc.ping_pong_service.PingPongServiceGrpc;
import com.demo.grpc.ping_pong_service.PingRequest;
import com.demo.grpc.ping_pong_service.PongResponse;
import com.demo.grpc.ping_pong_service.ping_service.model.PingRestRequest;
import com.demo.grpc.ping_pong_service.ping_service.model.PongRestResponse;
import com.demo.grpc.ping_pong_service.ping_service.rest.PongServiceRestClient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.Random;
import java.util.stream.IntStream;

@Service
public class PingService {

    private static final int MAX_NUM = 1000;

    private static final Logger LOGGER = LoggerFactory.getLogger(PingService.class);

    private final MeterRegistry meterRegistry;
    private final PongServiceRestClient pongServiceRestClient;
    private final ManagedChannel channel;

    public PingService(MeterRegistry meterRegistry, PongServiceRestClient pongServiceRestClient) {
        this.meterRegistry = meterRegistry;
        this.pongServiceRestClient = pongServiceRestClient;
        this.channel = ManagedChannelBuilder.forAddress("localhost", 8092)
                .usePlaintext()
                .build();
    }

    public void sendPingWithGRPC() {
        LOGGER.info("start pingPong with gRPC");
        Timer.Sample sample = Timer.start();
        PingPongServiceGrpc.PingPongServiceBlockingStub stub = PingPongServiceGrpc.newBlockingStub(channel);
        for (int i=0; i < MAX_NUM; i++) {
            PongResponse pongResponse = stub.pingPong(PingRequest.newBuilder()
                    .setPing("ping" + getRandomInt())
                    .build());
            String pong = pongResponse.getPong();
        }

        sample.stop(meterRegistry.timer("sample", Collections.singletonList(new ImmutableTag("method", "gRPC"))));
        LOGGER.info("stop pingPong with gRPC");
    }

    public void sendPingWithHttp() {
        LOGGER.info("start pingPong with HTTP");
        Timer.Sample sample = Timer.start();
        for (int i=0; i < MAX_NUM; i++) {
            PongRestResponse adderResponse = pongServiceRestClient.pingPong(PingRestRequest.builder()
                    .ping("ping" + getRandomInt())
                    .build());
            String pong = adderResponse.getPong();
        }

        sample.stop(meterRegistry.timer("sample", Collections.singletonList(new ImmutableTag("method", "HTTP"))));
        LOGGER.info("stop pingPong with HTTP");
    }

    private int getRandomInt() {
        return (int)(Math.random() * Integer.MAX_VALUE);
    }

    @PreDestroy
    public void preDestroy() {
        channel.shutdown();
    }

}
