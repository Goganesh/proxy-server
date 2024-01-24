package com.goganesh.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProxyServer {

    public static void main(String[] args) {
        SpringApplication.run(ProxyServer.class, args);
    }
}
