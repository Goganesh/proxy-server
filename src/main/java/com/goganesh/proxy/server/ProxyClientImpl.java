package com.goganesh.proxy.server;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

public class ProxyClientImpl implements ProxyClient {

    private final RestTemplate restTemplate;
    private final String serverName;

    private volatile LocalDateTime lastLunchTime = LocalDateTime.now();

    public ProxyClientImpl(String username, String password, String proxyUrl, int port) {
        final SimpleClientHttpRequestFactory proxyFactory = new SimpleClientHttpRequestFactory();
        final InetSocketAddress address = new InetSocketAddress(proxyUrl, port);
        final Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
        proxyFactory.setProxy(proxy);

        final BufferingClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(proxyFactory);
        this.restTemplate = new RestTemplateBuilder()
                .requestFactory(() -> factory)
                .defaultHeader(HttpHeaders.PROXY_AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((username + ':' + password).getBytes(StandardCharsets.UTF_8)))
                .build();

        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new LoggingInterceptor());
        restTemplate.setInterceptors(interceptors);

        this.serverName = username + ":" + port;
    }

    @Override
    public <T> ResponseEntity<T> sendRequest(URI uri, HttpMethod method, HttpEntity<?> requestEntity, Class<T> tClass) {
        lastLunchTime = LocalDateTime.now();
        return restTemplate.exchange(uri, method, requestEntity, tClass);
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public LocalDateTime lastLunchTime() {
        return lastLunchTime;
    }
}
