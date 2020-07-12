package com.ze.crawler.core.service.executor;

import com.ze.crawler.core.constants.Constant;
import com.ze.crawler.core.constants.Dictionary;
import com.ze.crawler.core.constants.WKConstant;
import com.ze.crawler.core.entity.*;
import com.ze.crawler.core.model.TeamFilterModel;
import com.ze.crawler.core.repository.*;
import com.ze.crawler.core.service.*;
import com.ze.crawler.core.service.log.LogService;
import com.ze.crawler.core.service.water.WaterCalculator;
import com.ze.crawler.core.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 体育爬虫执行器
 */
@Slf4j
@Service
public class SportsExecutor {
    @Autowired
    private LogService logService;
    @Autowired
    private PbSportsService pbSportsService;
    @Autowired
    private ImSportsService imSportsService;
    @Autowired
    private YbbSportsService ybbSportsService;
    @Autowired
    private BtiSportService btiSportService;
    @Autowired
    private PbSportsRepository pbSportsRepository;
    @Autowired
    private ImSportsRepository imSportsRepository;
    @Autowired
    private YbbSportsRepository ybbSportsRepository;
    @Autowired
    private BtiSportsRepository btiSportsRepository;
    @Autowired
    private WaterCalculator waterCalculator;

    // 线程池
    private final static ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 执行器
     * @param appointedLeagues  指定联赛
     * @param appointedTeams    指定队伍
     * @param threshold         阈值
     * @param main              指定主盘口
     */
    public void executor(String taskId, String type, Set<String> appointedLeagues, List<TeamFilterModel> appointedTeams,
                         double threshold, Integer main) {
        List<Callable<CrawlerThread>> threads = new ArrayList<>();
        CrawlerThread pb = new CrawlerThread(taskId, type, appointedLeagues, appointedTeams, pbSportsService);
        CrawlerThread im = new CrawlerThread(taskId, type, appointedLeagues, appointedTeams, imSportsService);
        CrawlerThread ybb = new CrawlerThread(taskId, type, appointedLeagues, appointedTeams, ybbSportsService);
        CrawlerThread bti = new CrawlerThread(taskId, type, appointedLeagues, appointedTeams, btiSportService);
        threads.add(pb);
        threads.add(im);
        threads.add(ybb);
        threads.add(bti);

        try {
            // 执行
            executorService.invokeAll(threads, Constant.EXECUTOR_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logService.log(Constant.LOG_TYPE_SPORTS_INVOKE_ERROR, "sports", taskId + "_" + type, e);
        }

        try {
            long startTime = System.currentTimeMillis();

            // 执行完成, 进行水量计算
            List<PbSports> pbSportsList = pbSportsRepository.findByTaskId(taskId);
            List<ImSports> imSportsList = imSportsRepository.findByTaskId(taskId);
            List<YbbSports> ybbSportsList = ybbSportsRepository.findByTaskId(taskId);
            List<BtiSports> btiSportsList = btiSportsRepository.findByTaskId(taskId);
            Map<Integer, List<? extends Sports>> esportsMap = new LinkedHashMap<>();
            esportsMap.put(Constant.SPORTS_DISH_PB, pbSportsList);
            esportsMap.put(Constant.SPORTS_DISH_IM, imSportsList);
            esportsMap.put(Constant.SPORTS_DISH_YB, ybbSportsList);
            esportsMap.put(Constant.SPORTS_DISH_BTI, btiSportsList);

            Map<Integer, List<? extends Sports>> sportsMapOrder = new LinkedHashMap<>();
            if (main != null) {
                // 指定主盘口, 进行排序
                if (main.equals(Constant.SPORTS_DISH_PB)) {
                    sportsMapOrder.put(Constant.SPORTS_DISH_PB, esportsMap.get(Constant.SPORTS_DISH_PB));
                } else if (main.equals(Constant.SPORTS_DISH_IM)) {
                    sportsMapOrder.put(Constant.SPORTS_DISH_IM, esportsMap.get(Constant.SPORTS_DISH_IM));
                } else if (main.equals(Constant.SPORTS_DISH_YB)) {
                    sportsMapOrder.put(Constant.SPORTS_DISH_YB, esportsMap.get(Constant.SPORTS_DISH_YB));
                } else if (main.equals(Constant.SPORTS_DISH_BTI)) {
                    sportsMapOrder.put(Constant.SPORTS_DISH_BTI, esportsMap.get(Constant.SPORTS_DISH_BTI));
                }
                for (Integer key : esportsMap.keySet()) {
                    sportsMapOrder.put(key, esportsMap.get(key));
                }
            } else {
                sportsMapOrder = esportsMap;
            }

            // 报水
            waterCalculator.calculateWater(Dictionary.SPORT_DISH_TYPE_MAPPING, sportsMapOrder, threshold, main, appointedLeagues == null ? WKConstant.SEND_TYPE_SPORTS : WKConstant.SEND_TYPE_SPORTS_BP);

            long endTime = System.currentTimeMillis();
            log.info("报水_" + type + "_" + taskId + "_[耗时（秒）: " + CommonUtils.getSeconds(endTime - startTime) + "]");
        } catch (Exception e) {
            logService.log(Constant.LOG_TYPE_SPORTS_WATER_CALCULATE_ERROR, "sports", taskId + "_" + type, e);
        }
    }

    /**
     * 内部类 - 爬虫线程
     */
    static class CrawlerThread implements Callable<CrawlerThread> {

        private String taskId;
        private String type;
        private Set<String> appointedLeagues;
        private List<TeamFilterModel> appointedTeams;
        private BaseService baseService;

        public CrawlerThread(String taskId, String type, Set<String> appointedLeagues, List<TeamFilterModel> appointedTeams, BaseService baseService) {
            this.taskId = taskId;
            this.type = type;
            this.appointedLeagues = appointedLeagues;
            this.appointedTeams = appointedTeams;
            this.baseService = baseService;
        }

        @Override
        public CrawlerThread call() {
            baseService.crawler(taskId, type, appointedLeagues, appointedTeams);
            return null;
        }
    }
}
