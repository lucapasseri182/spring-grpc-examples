package com.demo.grpc.ping_pong_service.pong_service.service;

import org.springframework.stereotype.Service;

@Service
public class PongService {

    public String pingPong(String ping) {
        return ping.replace("ping", "pong");
    }

}
