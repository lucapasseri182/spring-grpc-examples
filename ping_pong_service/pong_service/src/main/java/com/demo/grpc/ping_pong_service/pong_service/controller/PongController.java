package com.demo.grpc.ping_pong_service.pong_service.controller;

import com.demo.grpc.ping_pong_service.pong_service.model.PingRestRequest;
import com.demo.grpc.ping_pong_service.pong_service.model.PongRestResponse;
import com.demo.grpc.ping_pong_service.pong_service.service.PongService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PongController {

    private final PongService pongService;

    public PongController(PongService pongService) {
        this.pongService = pongService;
    }

    @PostMapping("/pingPong")
    public PongRestResponse pingPong(@RequestBody PingRestRequest pingRestRequest) {
        String pong = pongService.pingPong(pingRestRequest.getPing());
        return PongRestResponse.builder()
                .pong(pong)
                .build();
    }

}
