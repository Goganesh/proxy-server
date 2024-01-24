package com.goganesh.proxy.server;

import org.springframework.http.ResponseEntity;

public interface ProxyService {

    ResponseEntity<String> sendGetRequestToProxy(String url);

    ResponseEntity<String> sendPostRequestToProxy(String url,
                                                  String body,
                                                  String encoding,
                                                  String data);
}
