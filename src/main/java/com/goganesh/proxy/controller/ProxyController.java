package com.goganesh.proxy.controller;

import com.goganesh.proxy.server.ProxyService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/proxy")
@AllArgsConstructor
public class ProxyController {

    private final ProxyService proxyService;

    @GetMapping
    public ResponseEntity<String> sendGetRequestToProxy(@RequestParam String url) {
        return proxyService.sendGetRequestToProxy(url);
    }

    @PostMapping
    public ResponseEntity<String> sendPostRequestToProxy(@RequestParam String url,
                                                         @RequestParam(defaultValue = MediaType.APPLICATION_FORM_URLENCODED_VALUE) String encoding,
                                                         @RequestParam String data) {
        return proxyService.sendPostRequestToProxy(url, data, encoding, data);
    }
}
