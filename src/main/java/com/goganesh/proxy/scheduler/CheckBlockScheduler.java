package com.goganesh.proxy.scheduler;

import com.goganesh.proxy.server.BlockProxyClientService;
import com.goganesh.proxy.server.ConfigConstant;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CheckBlockScheduler {

    private final BlockProxyClientService blockProxyClientService;

    @Scheduled(cron = "${" + ConfigConstant.CHECK_BLOCKING_INTERVAL_CRON_CONFIG + "}")
    public void checkBlocks() {
        blockProxyClientService.checkBlocks();
    }
}
