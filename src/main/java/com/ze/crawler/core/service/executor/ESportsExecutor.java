package com.ze.crawler.core.service.executor;

import com.ze.crawler.core.constants.Constant;
import com.ze.crawler.core.constants.WkConstant;
import com.ze.crawler.core.entity.*;
import com.ze.crawler.core.model.TeamFilterModel;
import com.ze.crawler.core.repository.ImEsportsRepository;
import com.ze.crawler.core.repository.PbEsportsRepository;
import com.ze.crawler.core.repository.RgEsportsRepository;
import com.ze.crawler.core.repository.TfEsportsRepository;
import com.ze.crawler.core.service.*;
import com.ze.crawler.core.service.log.LogService;
import com.ze.crawler.core.service.water.WaterCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 电竞爬虫执行器
 */
@Service
public class ESportsExecutor {
    @Autowired
    private LogService logService;
    @Autowired
    private PbESportsService pbESportsService;
    @Autowired
    private RgESportsService rgESportsService;
    @Autowired
    private TfESportsService tfESportsService;
    @Autowired
    private ImESportsService imESportsService;
    @Autowired
    private PbEsportsRepository pbEsportsRepository;
    @Autowired
    private RgEsportsRepository rgEsportsRepository;
    @Autowired
    private TfEsportsRepository tfEsportsRepository;
    @Autowired
    private ImEsportsRepository imEsportsRepository;
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
        CrawlerThread pb = new CrawlerThread(taskId, type, appointedLeagues, appointedTeams, pbESportsService);
        CrawlerThread rg = new CrawlerThread(taskId, type, appointedLeagues, appointedTeams, rgESportsService);
        CrawlerThread tf = new CrawlerThread(taskId, type, appointedLeagues, appointedTeams, tfESportsService);
        CrawlerThread im = new CrawlerThread(taskId, type, appointedLeagues, appointedTeams, imESportsService);
        threads.add(pb);
        threads.add(rg);
        threads.add(tf);
        threads.add(im);

        try {
            // 执行
            executorService.invokeAll(threads, Constant.EXECUTOR_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logService.log(Constant.LOG_TYPE_ESPORTS_INVOKE_ERROR, "esports", taskId + "_" + type, e);
        }

        try {
            // 执行完成, 进行水量计算
            List<PbEsports> pbEsportsList = pbEsportsRepository.findByTaskId(taskId);
            List<RgEsports> rgEsportsList = rgEsportsRepository.findByTaskId(taskId);
            List<TfEsports> tfEsportsList = tfEsportsRepository.findByTaskId(taskId);
            List<ImEsports> imEsportsList = imEsportsRepository.findByTaskId(taskId);
            Map<Integer, List<? extends Sports>> esportsMap = new LinkedHashMap<>();
            esportsMap.put(Constant.ESPORTS_DISH_PB, pbEsportsList);
            esportsMap.put(Constant.ESPORTS_DISH_RG, rgEsportsList);
            esportsMap.put(Constant.ESPORTS_DISH_TF, tfEsportsList);
            esportsMap.put(Constant.ESPORTS_DISH_IM, imEsportsList);

            Map<Integer, List<? extends Sports>> esportsMapOrder = new LinkedHashMap<>();
            if (main != null) {
                // 指定主盘口, 进行排序
                if (main.equals(Constant.ESPORTS_DISH_PB)) {
                    esportsMapOrder.put(Constant.ESPORTS_DISH_PB, esportsMap.get(Constant.ESPORTS_DISH_PB));
                } else if (main.equals(Constant.ESPORTS_DISH_RG)) {
                    esportsMapOrder.put(Constant.ESPORTS_DISH_RG, esportsMap.get(Constant.ESPORTS_DISH_RG));
                } else if (main.equals(Constant.ESPORTS_DISH_TF)) {
                    esportsMapOrder.put(Constant.ESPORTS_DISH_TF, esportsMap.get(Constant.ESPORTS_DISH_TF));
                } else if (main.equals(Constant.ESPORTS_DISH_IM)) {
                    esportsMapOrder.put(Constant.ESPORTS_DISH_IM, esportsMap.get(Constant.ESPORTS_DISH_IM));
                }
                for (Integer key : esportsMap.keySet()) {
                    esportsMapOrder.put(key, esportsMap.get(key));
                }
            } else {
                esportsMapOrder = esportsMap;
            }

            // 报水
            waterCalculator.calculateWater(esportsMapOrder, threshold, main, appointedLeagues == null ? WkConstant.ESPORTS_YL : WkConstant.ESPORTS_BP);
        } catch (Exception e) {
            logService.log(Constant.LOG_TYPE_ESPORTS_WATER_CALCULATE_ERROR, "esports", taskId + "_" + type, e);
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
