package com.ze.crawler.controller;

import com.ze.crawler.core.service.proxy.ProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/proxy")
public class ProxyController {
    @Autowired
    private ProxyService proxyService;

    /**
     * 切换代理IP
     */
    @RequestMapping("/switch")
    public boolean switchProxyIp() {
        proxyService.getProxyIp();
        return true;
    }
}
