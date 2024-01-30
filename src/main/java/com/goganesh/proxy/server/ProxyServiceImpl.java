package com.goganesh.proxy.server;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ProxyServiceImpl implements ProxyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyServiceImpl.class);

    private static final HttpStatus ALL_PROXY_BAN_CODE = HttpStatus.I_AM_A_TEAPOT;
    private static final List<Integer> REDIRECT_CODES = Arrays.asList(301, 302, 307);

    private final BlockProxyClientService blockProxyClientService;
    private final ProxyClientHolder proxyClientHolder;

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
        Optional<ProxyClient> clientOptional = proxyClientHolder.getProxyClient();

        ResponseEntity<T> response;
        if (clientOptional.isPresent()) {
            ProxyClient client = clientOptional.get();
            try {
                response = client.sendRequest(uri, method, requestEntity, tClass);
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                blockProxyClientService.block(client);
                response = handleRequest(uri, method, requestEntity, tClass);
            }
            HttpStatusCode code = response.getStatusCode();
            if (REDIRECT_CODES.contains(code.value())) {
                URI newUri = response.getHeaders()
                        .getLocation();
                response = handleRequest(newUri, method, requestEntity, tClass);
            } else if (code.is5xxServerError()) {
                blockProxyClientService.block(client);
                response = handleRequest(uri, method, requestEntity, tClass);
            }

        } else {
            response = new ResponseEntity<>(ALL_PROXY_BAN_CODE);
        }

        return response;
    }
}
