package com.ze.crawler.core.component;

import com.ze.crawler.controller.WaterController;
import com.ze.crawler.core.constants.Constant;
import com.ze.crawler.core.entity.WaterControl;
import com.ze.crawler.core.model.TeamFilterModel;
import com.ze.crawler.core.repository.WaterControlRepository;
import com.ze.crawler.core.service.executor.ESportsExecutor;
import com.ze.crawler.core.service.executor.SportsExecutor;
import com.ze.crawler.core.utils.LangUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * 临时爬虫任务V2 - 异步执行
 */
@Component
public class CrawlerTaskV2 {
    @Autowired
    private ESportsExecutor eSportsExecutor;
    @Autowired
    private SportsExecutor sportsExecutor;
    @Autowired
    private WaterControlRepository waterControlRepository;

    // 水量控制
    public final static String YL_CONTROL_ID = "00000"; // 电竞 - 娱乐
    public final static String BP_CONTROL_ID = "11111"; // 电竞 - 包赔
    public final static String TY_CONTROL_ID = "22222"; // 体育

    /* 电竞 */

    @Scheduled(initialDelay = 1000 * 30, fixedDelay = 1000 * 60 * 5)
    public void esportRun() {
        System.out.println("电竞 执行开始");

        System.out.println("电竞包赔 执行开始");
        WaterControl waterControl = waterControlRepository.getOne(BP_CONTROL_ID);
        if (WaterController.ENABLE_ON.equals(waterControl.getEnable())) {
            double threshold = Double.parseDouble(waterControl.getThreshold());

            if ("0".equals(waterControl.getTeamA()) || "0".equals(waterControl.getTeamB())) {
                String league = waterControl.getLeague();
                List<String> leagueList = Arrays.asList(league.split(","));
                if (!CollectionUtils.isEmpty(leagueList)) {
                    Map<String, Set<String>> map = new HashMap<>();
                    map.put(Constant.ESPORTS_TYPE_LOL, new HashSet<>());
                    map.put(Constant.ESPORTS_TYPE_DOTA2, new HashSet<>());
                    map.put(Constant.ESPORTS_TYPE_CSGO, new HashSet<>());
                    map.put(Constant.ESPORTS_TYPE_KPL, new HashSet<>());

                    for (String l : leagueList) {
                        if (l.startsWith("2")) {
                            map.get(Constant.ESPORTS_TYPE_DOTA2).add(l);
                        } else if (l.startsWith("3")) {
                            map.get(Constant.ESPORTS_TYPE_CSGO).add(l);
                        } else if (l.startsWith("4")) {
                            map.get(Constant.ESPORTS_TYPE_KPL).add(l);
                        } else if (l.startsWith("1")) {
                            map.get(Constant.ESPORTS_TYPE_LOL).add(l);
                        }
                    }

                    for (String type : map.keySet()) {
                        if (!CollectionUtils.isEmpty(map.get(type))) {
                            eSportsExecutor.executor(LangUtils.generateUuid(), type,
                                    map.get(type), null, threshold, null);
                        }
                    }
                }
            } else {
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
        }
        System.out.println("电竞包赔 执行完成");

        System.out.println("电竞娱乐 执行开始");
        WaterControl ylWaterControl = waterControlRepository.getOne(YL_CONTROL_ID);
        if (WaterController.ENABLE_ON.equals(ylWaterControl.getEnable())) {
            double threshold = Double.parseDouble(ylWaterControl.getThreshold());

            List<String> esportType = new ArrayList<>();
            esportType.add(Constant.ESPORTS_TYPE_LOL);
            esportType.add(Constant.ESPORTS_TYPE_DOTA2);
            esportType.add(Constant.ESPORTS_TYPE_CSGO);
            esportType.add(Constant.ESPORTS_TYPE_KPL);

            for (String type : esportType) {
                eSportsExecutor.executor(LangUtils.generateUuid(), type, null, null, threshold, null);

                System.out.println(type + " 执行完成");
            }
        }
        System.out.println("电竞娱乐 执行完成");

        System.out.println("电竞 执行完成");
    }

    /* 体育 */

    /**
     * 足球
     */
    @Scheduled(initialDelay = 1000 * 30, fixedDelay = 1000 * 60 * 5)
    public void sportRun() {
        System.out.println("体育 执行开始");

        WaterControl waterControl = waterControlRepository.getOne(TY_CONTROL_ID);
        if (WaterController.ENABLE_ON.equals(waterControl.getEnable())) {
            double threshold = Double.parseDouble(waterControl.getThreshold());

            List<String> sportType = new ArrayList<>();
            sportType.add(Constant.SPORTS_TYPE_SOCCER);
            sportType.add(Constant.SPORTS_TYPE_BASKETBALL);

            for (String type : sportType) {
                sportsExecutor.executor(LangUtils.generateUuid(), type, null, null, threshold, null);

                System.out.println(type + " 执行完成");
            }
        }
        System.out.println("体育 执行完成");
    }
}
