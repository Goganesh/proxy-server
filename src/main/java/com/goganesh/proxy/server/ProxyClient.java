package com.goganesh.proxy.server;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.time.LocalDateTime;

public interface ProxyClient {

    <T> ResponseEntity<T> sendRequest(URI uri, HttpMethod method, HttpEntity<?> requestEntity, Class<T> tClass);
    String getServerName();
    LocalDateTime lastLunchTime();
}
