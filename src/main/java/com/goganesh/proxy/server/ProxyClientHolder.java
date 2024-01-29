package com.goganesh.proxy.server;

import java.util.Optional;

public interface ProxyClientHolder {
    Optional<ProxyClient> getProxyClient();
}
