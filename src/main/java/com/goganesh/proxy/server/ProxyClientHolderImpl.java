package com.goganesh.proxy.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ProxyClientHolderImpl implements ProxyClientHolder {

    private final BlockProxyClientService blockProxyClientService;
    private final List<ProxyClient> clients = new CopyOnWriteArrayList<>();

    public ProxyClientHolderImpl(@Value("#{'${" + ConfigConstant.PROXY_SERVER_CONFIG + "}'.split(',')}") List<String> proxyServerParams,
                                 BlockProxyClientService blockProxyClientService) {
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
    public Optional<ProxyClient> getProxyClient() {
        return clients.stream()
                .filter(client -> !blockProxyClientService.isBlocked(client))
                .sorted(Comparator.comparing(ProxyClient::lastLunchTime))
                .findFirst();
    }
}
