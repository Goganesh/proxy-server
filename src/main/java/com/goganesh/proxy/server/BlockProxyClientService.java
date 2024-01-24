package com.goganesh.proxy.server;

public interface BlockProxyClientService {

    boolean isBlocked(ProxyClient proxyClient);
    void block(ProxyClient client);
    void checkBlocks();
}
