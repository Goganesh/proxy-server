package com.goganesh.proxy.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BlockProxyClientServiceImpl implements BlockProxyClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockProxyClientServiceImpl.class);

    private final Map<ProxyClient, LocalDateTime> blocks = new ConcurrentHashMap<>();
    private final int blockingPeriodMinutes;

    public BlockProxyClientServiceImpl(@Value("${" + ConfigConstant.BLOCKING_PERIOD_MINUTES_CONFIG + "}") Integer blockingPeriodMinutes) {
        this.blockingPeriodMinutes = blockingPeriodMinutes;
    }

    @Override
    public boolean isBlocked(ProxyClient proxyClient) {
        return blocks.containsKey(proxyClient);
    }

    @Override
    public void block(ProxyClient client) {
        LOGGER.info(String.format("Proxу server %s is blocked", client.getServerName()));
        blocks.put(client, LocalDateTime.now());
    }

    @Override
    public void checkBlocks() {
        blocks.entrySet()
                .stream()
                .filter(entry -> entry.getValue().plusMinutes(blockingPeriodMinutes).isBefore(LocalDateTime.now()))
                .map(Map.Entry::getKey)
                .forEach(client -> {
                    LOGGER.info(String.format("Proxу server %s is unblocked", client.getServerName()));
                    blocks.remove(client);
                });
    }
}
