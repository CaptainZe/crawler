package com.ze.crawler.controller;

import com.ze.crawler.core.component.CrawlerTask;
import com.ze.crawler.core.entity.ALog;
import com.ze.crawler.core.repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
