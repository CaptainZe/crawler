package com.ze.crawler.controller;

import com.ze.crawler.core.component.CrawlerTask;
import com.ze.crawler.core.entity.ALog;
import com.ze.crawler.core.repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 临时报水控制器
 */
@RestController
@RequestMapping("/water")
public class WaterController {
    @Autowired
    private LogRepository logRepository;

    @RequestMapping("/init")
    public boolean initThreshold(@RequestParam String threshold) {
        ALog aLog = new ALog();
        aLog.setId(CrawlerTask.THRESHOLD_ID);
        aLog.setFromDish(threshold);
        logRepository.save(aLog);

        ALog aLog2 = new ALog();
        aLog2.setId(CrawlerTask.SWITCH_ID);
        aLog2.setFromDish("1");
        logRepository.save(aLog2);

        ALog aLog3 = new ALog();
        aLog3.setId(CrawlerTask.APPOINTED_ID);
        aLog3.setFromDish("0");
        logRepository.save(aLog3);
        return true;
    }

    @RequestMapping("/change")
    public boolean changeThreshold(@RequestParam String threshold) {
        ALog aLog = logRepository.getOne(CrawlerTask.THRESHOLD_ID);
        aLog.setFromDish(threshold);
        logRepository.save(aLog);
        return true;
    }

    @RequestMapping("/task")
    public boolean taskSwitch(@RequestParam String open) {
        ALog aLog = logRepository.getOne(CrawlerTask.SWITCH_ID);
        aLog.setFromDish(open);
        logRepository.save(aLog);
        return true;
    }

    @RequestMapping("/appointed")
    public boolean appointed(@RequestParam String league, @RequestParam String teamA, @RequestParam String teamB) {
        ALog aLog = logRepository.getOne(CrawlerTask.APPOINTED_ID);
        aLog.setFromDish(league);
        aLog.setData(teamA);
        aLog.setMsg(teamB);
        logRepository.save(aLog);
        return true;
    }
}
