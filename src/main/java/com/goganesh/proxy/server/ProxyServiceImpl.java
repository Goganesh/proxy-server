package com.goganesh.proxy.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ProxyServiceImpl implements ProxyService {

    private static final HttpStatus ALL_PROXY_BAN_CODE = HttpStatus.I_AM_A_TEAPOT;
    private static final List<HttpStatusCode> REDIRECT_CODES = Arrays.asList(HttpStatus.MOVED_PERMANENTLY, HttpStatus.FOUND, HttpStatusCode.valueOf(311));

    private final List<ProxyClient> clients = new CopyOnWriteArrayList<>();
    private final BlockProxyClientServiceImpl blockProxyClientService;

    public ProxyServiceImpl(@Value("#{'${" + ConfigConstant.PROXY_SERVER_CONFIG + "}'.split(',')}") List<String> proxyServerParams,
                            BlockProxyClientServiceImpl blockProxyClientService) {
        if (proxyServerParams.isEmpty()) {
            throw new IllegalStateException(String.format("Define property %s for proxy servers", ConfigConstant.PROXY_SERVER_CONFIG));
        }
        proxyServerParams.forEach(param -> {
            String[] params = param.split(":");
            if (params.length != 4) {
                throw new IllegalStateException(String.format("Config %s has wrong format", ConfigConstant.PROXY_SERVER_CONFIG));
            }
            String username = params[2];
            String password = params[3];
            int port = Integer.parseInt(params[1]);
            String proxyUrl = params[0];
            this.clients.add(new ProxyClientImpl(username, password, proxyUrl, port));
        });

        this.blockProxyClientService = blockProxyClientService;
    }

    @Override
    public ResponseEntity<String> sendGetRequestToProxy(String url) {
        URI uri = UriComponentsBuilder.fromUriString(url)
                .build()
                .toUri();
        HttpMethod method = HttpMethod.GET;
        HttpHeaders headers = new HttpHeaders();
        MimicUserUtil.mimicHeader(headers);
        HttpEntity<String> request = new HttpEntity<>(null, headers);

        return handleRequest(uri, method, request, String.class);
    }

    @Override
    public ResponseEntity<String> sendPostRequestToProxy(String url, String body, String encoding, String data) {
        URI uri = UriComponentsBuilder.fromUriString(url)
                .build()
                .toUri();
        HttpMethod method = HttpMethod.POST;
        HttpHeaders headers = new HttpHeaders();
        MimicUserUtil.mimicHeader(headers);
        headers.set(HttpHeaders.CONTENT_TYPE, encoding);
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        return handleRequest(uri, method, request, String.class);
    }

    private <T> ResponseEntity<T> handleRequest(URI uri, HttpMethod method, HttpEntity<?> requestEntity, Class<T> tClass) {
        Optional<ProxyClient> clientOptional = clients.stream()
                .filter(client -> !blockProxyClientService.isBlocked(client))
                .sorted(Comparator.comparing(ProxyClient::lastLunchTime))
                .findFirst();

        ResponseEntity<T> response;
        if (clientOptional.isPresent()) {
            ProxyClient client = clientOptional.get();
            response = client.sendRequest(uri, method, requestEntity, tClass);
            HttpStatusCode code = response.getStatusCode();
            if (REDIRECT_CODES.contains(code)) {
                URI newUri = response.getHeaders()
                        .getLocation();
                handleRequest(newUri, method, requestEntity, tClass);
            } else if (code.is5xxServerError()) {
                blockProxyClientService.block(client);
                handleRequest(uri, method, requestEntity, tClass);
            }
        } else {
            response = new ResponseEntity<>(ALL_PROXY_BAN_CODE);
        }

        return response;
    }
}
