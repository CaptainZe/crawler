package com.ze.crawler.controller;

import com.ze.crawler.core.constants.Constant;
import com.ze.crawler.core.constants.ProxyConstant;
import com.ze.crawler.core.service.proxy.ProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SuppressWarnings("all")
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
    public boolean switchProxyEnable(@RequestParam String open, @RequestParam(required = false) List<Integer> dish) {
        if (ENABLE_ON.equals(open)) {
            ProxyConstant.USE_PROXY = true;
            
            if (CollectionUtils.isEmpty(dish)) {
                ProxyConstant.DISH_USE_PROXY.put(Constant.ESPORTS_DISH_PB, true);
                ProxyConstant.DISH_USE_PROXY.put(Constant.ESPORTS_DISH_RG, true);
                ProxyConstant.DISH_USE_PROXY.put(Constant.ESPORTS_DISH_TF, true);
                ProxyConstant.DISH_USE_PROXY.put(Constant.ESPORTS_DISH_IM, true);
                ProxyConstant.DISH_USE_PROXY.put(Constant.ESPORTS_DISH_FY, true);

                ProxyConstant.DISH_USE_PROXY.put(Constant.SPORTS_DISH_PB, true);
                ProxyConstant.DISH_USE_PROXY.put(Constant.SPORTS_DISH_IM, true);
                ProxyConstant.DISH_USE_PROXY.put(Constant.SPORTS_DISH_YB, true);
                ProxyConstant.DISH_USE_PROXY.put(Constant.SPORTS_DISH_SB, true);
                ProxyConstant.DISH_USE_PROXY.put(Constant.SPORTS_DISH_BTI, true);
            } else {
                for (Integer dishId : dish) {
                    ProxyConstant.DISH_USE_PROXY.put(dishId, true);
                }
            }
            
            proxyService.getProxyIp(ProxyConstant.SCENE_ON_OPEN);
        } else {
            ProxyConstant.USE_PROXY = false;

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
        }
        return true;
    }

    /**
     * 切换代理IP
     */
    @RequestMapping("/switch_ip")
    public boolean switchProxyIp() {
        proxyService.getProxyIp(ProxyConstant.SCENE_ON_SWITCH);
        return true;
    }
}
