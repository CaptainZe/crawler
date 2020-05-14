package com.ze.crawler.core.component;

import com.ze.crawler.core.service.proxy.ProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ProxyTask {

    @Autowired
    private ProxyService proxyService;

    @Scheduled(cron = "0 0 0,6,12,18 * * ?")
    public void autoSwitchoverIp() {
        proxyService.getProxyIp();
    }
}
