package com.ze.crawler.core.component;

import com.ze.crawler.core.constants.Constant;
import com.ze.crawler.core.entity.ALog;
import com.ze.crawler.core.model.TeamFilterModel;
import com.ze.crawler.core.repository.LogRepository;
import com.ze.crawler.core.service.executor.ESportsExecutor;
import com.ze.crawler.core.utils.LangUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * 临时爬虫任务
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
    public final static String APPOINTED_ID = "22222";

    /**
     * LOL
     */
    @Scheduled(initialDelay = 1000 * 60, fixedDelay = 1000 * 60 * 5)
    public void lolTask() {
        ALog switchLog = logRepository.getOne(SWITCH_ID);
        if ("1".equals(switchLog.getFromDish())) {
            ALog aLog = logRepository.getOne(THRESHOLD_ID);
            double threshold = Double.parseDouble(aLog.getFromDish());

            ALog appointedLog = logRepository.getOne(APPOINTED_ID);
            if (!appointedLog.getFromDish().equals("0")) {
                TeamFilterModel teamFilterModel = new TeamFilterModel();
                teamFilterModel.setTeamOne(appointedLog.getData());
                teamFilterModel.setTeamTwo(appointedLog.getMsg());

                eSportsExecutor.executor(LangUtils.generateUuid(), Constant.ESPORTS_TYPE_LOL,
                        Collections.singleton(appointedLog.getFromDish()), Collections.singletonList(teamFilterModel), threshold, null);
            } else {
                eSportsExecutor.executor(LangUtils.generateUuid(), Constant.ESPORTS_TYPE_LOL, null, null, threshold, null);
            }
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

            ALog appointedLog = logRepository.getOne(APPOINTED_ID);
            if (!appointedLog.getFromDish().equals("0")) {
                TeamFilterModel teamFilterModel = new TeamFilterModel();
                teamFilterModel.setTeamOne(appointedLog.getData());
                teamFilterModel.setTeamTwo(appointedLog.getMsg());

                eSportsExecutor.executor(LangUtils.generateUuid(), Constant.ESPORTS_TYPE_DOTA2,
                        Collections.singleton(appointedLog.getFromDish()), Collections.singletonList(teamFilterModel), threshold, null);
            } else {
                eSportsExecutor.executor(LangUtils.generateUuid(), Constant.ESPORTS_TYPE_DOTA2, null, null, threshold, null);
            }
        }

        System.out.println("DOTA2 执行完成");
    }

    /**
     * CSGO
     */
    @Scheduled(initialDelay = 1000 * 60 * 3, fixedDelay = 1000 * 60 * 5)
    public void csTask() {
        ALog switchLog = logRepository.getOne(SWITCH_ID);
        if ("1".equals(switchLog.getFromDish())) {
            ALog aLog = logRepository.getOne(THRESHOLD_ID);
            double threshold = Double.parseDouble(aLog.getFromDish());

            ALog appointedLog = logRepository.getOne(APPOINTED_ID);
            if (!appointedLog.getFromDish().equals("0")) {
                TeamFilterModel teamFilterModel = new TeamFilterModel();
                teamFilterModel.setTeamOne(appointedLog.getData());
                teamFilterModel.setTeamTwo(appointedLog.getMsg());

                eSportsExecutor.executor(LangUtils.generateUuid(), Constant.ESPORTS_TYPE_CSGO,
                        Collections.singleton(appointedLog.getFromDish()), Collections.singletonList(teamFilterModel), threshold, null);
            } else {
                eSportsExecutor.executor(LangUtils.generateUuid(), Constant.ESPORTS_TYPE_CSGO, null, null, threshold, null);
            }
        }

        System.out.println("CSGO 执行完成");
    }
}
