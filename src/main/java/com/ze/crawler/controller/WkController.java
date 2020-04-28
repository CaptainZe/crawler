package com.ze.crawler.controller;

import com.ze.crawler.core.service.wk.WeiKongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/wk")
public class WkController {
    @Autowired
    private WeiKongService weiKongService;

    /**
     * 添加账号信息
     */
    @RequestMapping("/add")
    public void add(@RequestParam String wId, @RequestParam String wcId, @RequestParam String nickName, @RequestParam String targetWcId) {

        weiKongService.addWk(wId, wcId, nickName, targetWcId);
        weiKongService.refreshWkInfo();
    }

    /**
     * 添加账号信息
     */
    @RequestMapping("/update")
    public void update(@RequestParam String wId, @RequestParam String wcId, @RequestParam String nickName, @RequestParam String targetWcId) {

        weiKongService.updateWk(wId, wcId, nickName, targetWcId);
        weiKongService.refreshWkInfo();
    }

    @RequestMapping("/check")
    public Map<String, String> check() {
        Map<String, String> map = new HashMap<>();
        for (String wid : WeiKongService.WK_INFO.keySet()) {
            String s = weiKongService.sendTextByWid(wid, wid + ":OK");
            map.put(wid, s);
        }
        return map;
    }
}
