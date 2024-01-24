package com.goganesh.proxy.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.StringJoiner;

public class LoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        logRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        logResponse(response);

        return response;
    }

    private void logRequest(HttpRequest request, byte[] body) throws IOException {
        StringJoiner message = new StringJoiner("\n");
        message.add("");
        message.add("===log request start===");
        message.add(String.format("URI: %s", request.getURI()));
        message.add(String.format("Method: %s", request.getMethod()));
        message.add(String.format("Headers: %s", request.getHeaders()));
        message.add(String.format("Request body: %s", new String(body, "UTF-8")));
        message.add("===log request end===");
        LOGGER.info(message.toString());
    }

    private void logResponse(ClientHttpResponse response) throws IOException {
        StringJoiner message = new StringJoiner("\n");
        message.add("");
        message.add("===log response start===");
        message.add(String.format("Status code: %s", response.getStatusCode()));
        message.add(String.format("Status text: %s", response.getStatusText()));
        message.add(String.format("Headers: %s", response.getHeaders()));
        message.add(String.format("Response body: %s", StreamUtils.copyToString(response.getBody(), Charset.defaultCharset())));
        message.add("===log response end===");
        LOGGER.info(message.toString());
    }
}
