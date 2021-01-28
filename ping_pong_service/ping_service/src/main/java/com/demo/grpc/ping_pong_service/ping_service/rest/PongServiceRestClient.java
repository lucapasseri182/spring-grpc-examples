package com.demo.grpc.ping_pong_service.ping_service.rest;

import com.demo.grpc.ping_pong_service.ping_service.model.PingRestRequest;
import com.demo.grpc.ping_pong_service.ping_service.model.PongRestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "pongClient", url = "http://localhost:8082")
public interface PongServiceRestClient {

    @RequestMapping(method = RequestMethod.POST, value = "/pingPong", produces = MediaType.APPLICATION_JSON_VALUE)
    PongRestResponse pingPong(PingRestRequest adderRequest);

}