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
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Service
public class PingService {

    private static final int EPOCHS = 10;
    private static final int STANDARD_ITERATIONS = 1000;
    private static final int MASSIVE_ITERATIONS = 100;

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

    public void sendStandardPingsWithGRPC() {
        LOGGER.info("start standard pingPong with gRPC");
        for (int j = 0; j < EPOCHS; j++) {
            Timer.Sample sample = Timer.start();
            PingPongServiceGrpc.PingPongServiceBlockingStub stub = PingPongServiceGrpc.newBlockingStub(channel);
            for (int i = 0; i < STANDARD_ITERATIONS; i++) {
                PongResponse pongResponse = stub.pingPong(PingRequest.newBuilder()
                        .setPing("ping" + getRandomInt())
                        .build());
                String pong = pongResponse.getPong();
            }
            sample.stop(meterRegistry.timer("sample", Collections.singletonList(new ImmutableTag("method", "gRPC"))));
        }
        LOGGER.info("stop standard pingPong with gRPC");
    }

    public void sendStandardPingsWithHttp() {
        LOGGER.info("start standard pingPong with HTTP");
        for (int j = 0; j < EPOCHS; j++) {
            Timer.Sample sample = Timer.start();
            for (int i = 0; i < STANDARD_ITERATIONS; i++) {
                PongRestResponse adderResponse = pongServiceRestClient.pingPong(PingRestRequest.builder()
                        .ping("ping" + getRandomInt())
                        .build());
                String pong = adderResponse.getPong();
            }
            sample.stop(meterRegistry.timer("sample", Collections.singletonList(new ImmutableTag("method", "HTTP"))));
        }
        LOGGER.info("stop standard pingPong with HTTP");
    }

    public void sendMassivePingsWithGRPC() {
        LOGGER.info("start massive pingPong with gRPC");
        for (int j = 0; j < EPOCHS; j++) {
            Timer.Sample sample = Timer.start();
            PingPongServiceGrpc.PingPongServiceBlockingStub stub = PingPongServiceGrpc.newBlockingStub(channel);
            String ping = loadFile("massive-ping.txt");
            for (int i = 0; i < MASSIVE_ITERATIONS; i++) {
                PongResponse pongResponse = stub.pingPong(PingRequest.newBuilder()
                        .setPing(ping)
                        .build());
                String pong = pongResponse.getPong();
            }
            sample.stop(meterRegistry.timer("sample", Collections.singletonList(new ImmutableTag("method", "gRPC"))));
        }
        LOGGER.info("stop massive pingPong with gRPC");
    }

    public void sendMassivePingsWithHttp() {
        LOGGER.info("start massive pingPong with HTTP");
        for (int j = 0; j < EPOCHS; j++) {
            Timer.Sample sample = Timer.start();
            String ping = loadFile("massive-ping.txt");
            for (int i = 0; i < MASSIVE_ITERATIONS; i++) {
                PongRestResponse adderResponse = pongServiceRestClient.pingPong(PingRestRequest.builder()
                        .ping(ping)
                        .build());
                String pong = adderResponse.getPong();
            }
            sample.stop(meterRegistry.timer("sample", Collections.singletonList(new ImmutableTag("method", "HTTP"))));
        }
        LOGGER.info("stop massive pingPong with HTTP");
    }

    private int getRandomInt() {
        return (int)(Math.random() * Integer.MAX_VALUE);
    }

    private String loadFile(String fileName) {
        ClassPathResource cpr = new ClassPathResource(fileName);
        try {
            return new String(FileCopyUtils.copyToByteArray(cpr.getInputStream()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    @PreDestroy
    public void preDestroy() {
        channel.shutdown();
    }

}
