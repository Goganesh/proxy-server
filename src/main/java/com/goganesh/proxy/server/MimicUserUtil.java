package com.goganesh.proxy.server;

import org.springframework.http.HttpHeaders;

public class MimicUserUtil {

    public static void mimicHeader(HttpHeaders headers) {
        headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        headers.set(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.5");
        headers.set(HttpHeaders.REFERER, "http://google.com");
        headers.set(HttpHeaders.CONNECTION, "keep-alive");
        headers.set("Upgrade-Insecure-Requests", "1");
        headers.set("DNT", "1");
    }
}
