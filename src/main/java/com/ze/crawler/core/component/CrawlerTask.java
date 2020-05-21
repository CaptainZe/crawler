package com.ze.crawler.core.component;

import com.ze.crawler.controller.WaterController;
import com.ze.crawler.core.constants.Constant;
import com.ze.crawler.core.entity.WaterControl;
import com.ze.crawler.core.model.TeamFilterModel;
import com.ze.crawler.core.repository.WaterControlRepository;
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
    private WaterControlRepository waterControlRepository;

    // 水量控制
    public final static String YL_CONTROL_ID = "00000"; // 娱乐
    public final static String BP_CONTROL_ID = "11111"; // 包赔

    /**
     * 包赔
     */
    @Scheduled(initialDelay = 1000 * 30, fixedDelay = 1000 * 60 * 5)
    public void bpTask() {
        WaterControl waterControl = waterControlRepository.getOne(BP_CONTROL_ID);
        if (WaterController.ENABLE_ON.equals(waterControl.getEnable())) {
            double threshold = Double.parseDouble(waterControl.getThreshold());

            String type = Constant.ESPORTS_TYPE_LOL;
            String league = waterControl.getLeague();
            if (league.startsWith("2")) {
                type = Constant.ESPORTS_TYPE_DOTA2;
            } else if (league.startsWith("3")) {
                type = Constant.ESPORTS_TYPE_CSGO;
            } else if (league.startsWith("4")) {
                type = Constant.ESPORTS_TYPE_KPL;
            }

            TeamFilterModel teamFilterModel = new TeamFilterModel();
            teamFilterModel.setTeamOne(waterControl.getTeamA());
            teamFilterModel.setTeamTwo(waterControl.getTeamB());
            eSportsExecutor.executor(LangUtils.generateUuid(), type,
                        Collections.singleton(league), Collections.singletonList(teamFilterModel), threshold, null);
        }
        System.out.println("包赔 执行完成");
    }

    /* 娱乐使用 */
    /**
     * LOL
     */
    @Scheduled(initialDelay = 1000 * 60, fixedDelay = 1000 * 60 * 5)
    public void lolTask() {
        WaterControl waterControl = waterControlRepository.getOne(YL_CONTROL_ID);
        if (WaterController.ENABLE_ON.equals(waterControl.getEnable())) {
            double threshold = Double.parseDouble(waterControl.getThreshold());

            eSportsExecutor.executor(LangUtils.generateUuid(), Constant.ESPORTS_TYPE_LOL, null, null, threshold, null);
        }
        System.out.println("LOL 执行完成");
    }

    /**
     * DOTA2
     */
    @Scheduled(initialDelay = 1000 * 60 * 2, fixedDelay = 1000 * 60 * 5)
    public void dotaTask() {
        WaterControl waterControl = waterControlRepository.getOne(YL_CONTROL_ID);
        if (WaterController.ENABLE_ON.equals(waterControl.getEnable())) {
            double threshold = Double.parseDouble(waterControl.getThreshold());

            eSportsExecutor.executor(LangUtils.generateUuid(), Constant.ESPORTS_TYPE_DOTA2, null, null, threshold, null);
        }
        System.out.println("DOTA2 执行完成");
    }

    /**
     * CSGO
     */
    @Scheduled(initialDelay = 1000 * 60 * 3, fixedDelay = 1000 * 60 * 5)
    public void csTask() {
        WaterControl waterControl = waterControlRepository.getOne(YL_CONTROL_ID);
        if (WaterController.ENABLE_ON.equals(waterControl.getEnable())) {
            double threshold = Double.parseDouble(waterControl.getThreshold());

            eSportsExecutor.executor(LangUtils.generateUuid(), Constant.ESPORTS_TYPE_CSGO, null, null, threshold, null);
        }
        System.out.println("CSGO 执行完成");
    }

    /**
     * 王者荣耀
     */
    @Scheduled(initialDelay = 1000 * 60 * 4, fixedDelay = 1000 * 60 * 5)
    public void kplTask() {
        WaterControl waterControl = waterControlRepository.getOne(YL_CONTROL_ID);
        if (WaterController.ENABLE_ON.equals(waterControl.getEnable())) {
            double threshold = Double.parseDouble(waterControl.getThreshold());

            eSportsExecutor.executor(LangUtils.generateUuid(), Constant.ESPORTS_TYPE_KPL, null, null, threshold, null);
        }
        System.out.println("王者荣耀 执行完成");
    }
}
