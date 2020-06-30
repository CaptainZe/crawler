package com.ze.crawler.core.component;

import com.ze.crawler.core.constants.Constant;
import com.ze.crawler.core.constants.ProxyConstant;
import com.ze.crawler.core.service.proxy.ProxyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@SuppressWarnings("all")
@Slf4j
@Component
public class ProxyTask {

    @Autowired
    private ProxyService proxyService;

    @PostConstruct
    void initMapping() {
        ProxyConstant.DISH_USE_PROXY.put(Constant.ESPORTS_DISH_PB, false);
        ProxyConstant.DISH_USE_PROXY.put(Constant.ESPORTS_DISH_RG, false);
        ProxyConstant.DISH_USE_PROXY.put(Constant.ESPORTS_DISH_TF, false);
        ProxyConstant.DISH_USE_PROXY.put(Constant.ESPORTS_DISH_IM, false);
        ProxyConstant.DISH_USE_PROXY.put(Constant.ESPORTS_DISH_FY, false);

        ProxyConstant.DISH_USE_PROXY.put(Constant.SPORTS_DISH_PB, false);
        ProxyConstant.DISH_USE_PROXY.put(Constant.SPORTS_DISH_IM, false);
        ProxyConstant.DISH_USE_PROXY.put(Constant.SPORTS_DISH_YB, false);
        ProxyConstant.DISH_USE_PROXY.put(Constant.SPORTS_DISH_SB, false);
        ProxyConstant.DISH_USE_PROXY.put(Constant.SPORTS_DISH_BTI, false);
        log.info("代理开关初始化");
    }

    @Scheduled(cron = "0 0 0,6,12,18 * * ?")
    public void autoSwitchoverIp() {
        if (ProxyConstant.USE_PROXY) {
            proxyService.getProxyIp(ProxyConstant.SCENE_ON_TASK);
        }
    }
}
