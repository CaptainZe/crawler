package com.ze.crawler.controller;

import com.ze.crawler.core.constants.WKConstant;
import com.ze.crawler.core.service.wk.WeiKongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/wk")
public class WkController {
    @Autowired
    private WeiKongService weiKongService;

    /**
     * 测试微信是否可用
     */
    @RequestMapping("/check")
    public Map<String, String> check() {
        Map<String, String> map = new HashMap<>();
        for (String wid : WKConstant.WK_CHECK.keySet()) {
            String s = weiKongService.sendTextByWid(wid, wid + ":OK");
            map.put(wid, s);
        }
        return map;
    }

    /**
     * 全部登录
     */
    @RequestMapping("/login")
    public String login() {
        return weiKongService.reLoginAll();
    }
}
