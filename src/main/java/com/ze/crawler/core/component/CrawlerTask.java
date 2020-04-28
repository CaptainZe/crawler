package com.ze.crawler.core.component;

import com.ze.crawler.core.constants.Constant;
import com.ze.crawler.core.entity.ALog;
import com.ze.crawler.core.repository.LogRepository;
import com.ze.crawler.core.service.executor.ESportsExecutor;
import com.ze.crawler.core.utils.LangUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 临时爬虫
 */
@Component
public class CrawlerTask {
    @Autowired
    private ESportsExecutor eSportsExecutor;
    @Autowired
    private LogRepository logRepository;
    // 水量控制
    public final static String THRESHOLD_ID = "00000";
    public final static String SWITCH_ID = "11111";

    /**
     * LOL
     */
//    @Scheduled(initialDelay = 1000 * 60, fixedDelay = 1000 * 60 * 5)
    public void lolTask() {
        ALog switchLog = logRepository.getOne(SWITCH_ID);
        if ("1".equals(switchLog.getFromDish())) {
            ALog aLog = logRepository.getOne(THRESHOLD_ID);
            double threshold = Double.parseDouble(aLog.getFromDish());

            eSportsExecutor.executor(LangUtils.generateUuid(), Constant.ESPORTS_TYPE_LOL, threshold, null);
        }
        System.out.println("LOL 执行完成");
    }

    /**
     * DOTA2
     */
    @Scheduled(initialDelay = 1000 * 60 * 2, fixedDelay = 1000 * 60 * 5)
    public void dotaTask() {
        ALog switchLog = logRepository.getOne(SWITCH_ID);
        if ("1".equals(switchLog.getFromDish())) {
            ALog aLog = logRepository.getOne(THRESHOLD_ID);
            double threshold = Double.parseDouble(aLog.getFromDish());

            eSportsExecutor.executor(LangUtils.generateUuid(), Constant.ESPORTS_TYPE_DOTA2, threshold, null);
        }

        System.out.println("DOTA2 执行完成");
    }

    /**
     * CSGO
     */
//    @Scheduled(initialDelay = 1000 * 60 * 3, fixedDelay = 1000 * 60 * 5)
    public void csTask() {
        ALog switchLog = logRepository.getOne(SWITCH_ID);
        if ("1".equals(switchLog.getFromDish())) {
            ALog aLog = logRepository.getOne(THRESHOLD_ID);
            double threshold = Double.parseDouble(aLog.getFromDish());

            eSportsExecutor.executor(LangUtils.generateUuid(), Constant.ESPORTS_TYPE_CSGO, threshold, null);
        }

        System.out.println("CSGO 执行完成");
    }
}
