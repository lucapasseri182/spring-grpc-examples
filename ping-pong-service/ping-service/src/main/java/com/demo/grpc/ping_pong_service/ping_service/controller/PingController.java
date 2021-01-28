package com.demo.grpc.ping_pong_service.ping_service.controller;

import com.demo.grpc.ping_pong_service.ping_service.service.PingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    private final PingService pingService;

    public PingController(PingService pingService) {
        this.pingService = pingService;
    }

    @PostMapping("/start")
    public void start() {
        pingService.sendPingWithGRPC();
        pingService.sendPingWithHttp();
    }

}
