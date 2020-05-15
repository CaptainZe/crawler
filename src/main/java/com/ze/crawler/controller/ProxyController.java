package com.ze.crawler.controller;

import com.ze.crawler.core.constants.ProxyConstant;
import com.ze.crawler.core.service.proxy.ProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/proxy")
public class ProxyController {
    @Autowired
    private ProxyService proxyService;

    private final static String ENABLE_ON = "1";

    /**
     * 是否使用代理开关
     * @param open
     * @return
     */
    @RequestMapping("/enable")
    public boolean switchProxyEnable(@RequestParam String open) {
        if (ENABLE_ON.equals(open)) {
            ProxyConstant.USE_PROXY = true;

            proxyService.getProxyIp();
        } else {
            ProxyConstant.USE_PROXY = false;
        }
        return true;
    }

    /**
     * 切换代理IP
     */
    @RequestMapping("/switch_ip")
    public boolean switchProxyIp() {
        proxyService.getProxyIp();
        return true;
    }
}
