package com.ze.crawler.controller;

import com.ze.crawler.core.component.CrawlerTaskV2;
import com.ze.crawler.core.entity.WaterControl;
import com.ze.crawler.core.repository.WaterControlRepository;
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
    private WaterControlRepository waterControlRepository;

    // 开关
    public final static String ENABLE_ON = "1";
    public final static String ENABLE_OFF = "2";
    public final static String DEFAULT_THRESHOLD = "0";

    /**
     * 初始化
     * @return
     */
    @RequestMapping("/init")
    public boolean init() {
        WaterControl yl = new WaterControl();
        yl.setId(CrawlerTaskV2.YL_CONTROL_ID);
        yl.setEnable(ENABLE_OFF);
        yl.setThreshold(DEFAULT_THRESHOLD);
        waterControlRepository.save(yl);

        WaterControl bp = new WaterControl();
        bp.setId(CrawlerTaskV2.BP_CONTROL_ID);
        bp.setEnable(ENABLE_OFF);
        bp.setThreshold(DEFAULT_THRESHOLD);
        bp.setLeague("0");
        bp.setTeamA("0");
        bp.setTeamB("0");
        waterControlRepository.save(bp);

        WaterControl ty = new WaterControl();
        ty.setId(CrawlerTaskV2.TY_CONTROL_ID);
        ty.setEnable(ENABLE_OFF);
        ty.setThreshold(DEFAULT_THRESHOLD);
        waterControlRepository.save(ty);

        WaterControl bpty = new WaterControl();
        bpty.setId(CrawlerTaskV2.TY_BP_CONTROL_ID);
        bpty.setEnable(ENABLE_OFF);
        bpty.setThreshold(DEFAULT_THRESHOLD);
        bpty.setLeague("0");
        bpty.setTeamA("0");
        bpty.setTeamB("0");
        waterControlRepository.save(bpty);

        WaterControl djzd = new WaterControl();
        djzd.setId(CrawlerTaskV2.DJ_ZD_CONTROL_ID);
        djzd.setEnable(ENABLE_OFF);
        djzd.setThreshold(DEFAULT_THRESHOLD);
        waterControlRepository.save(djzd);

        return true;
    }

    /**
     * 电竞 - 娱乐报水控制器
     * @param open
     * @param threshold
     * @return
     */
    @RequestMapping("/yl_control")
    public boolean ylControl(@RequestParam String open, @RequestParam String threshold) {
        WaterControl waterControl = waterControlRepository.getOne(CrawlerTaskV2.YL_CONTROL_ID);
        waterControl.setEnable(open);
        waterControl.setThreshold(threshold);
        waterControlRepository.save(waterControl);
        return true;
    }

    /**
     * 电竞 - 包赔报水控制器
     * @param open
     * @param threshold
     * @param league
     * @param teamA
     * @param teamB
     * @return
     */
    @RequestMapping("/bp_control")
    public boolean bpControl(@RequestParam String open, @RequestParam String threshold, @RequestParam String league, @RequestParam String teamA, @RequestParam String teamB) {
        WaterControl waterControl = waterControlRepository.getOne(CrawlerTaskV2.BP_CONTROL_ID);
        waterControl.setEnable(open);
        waterControl.setThreshold(threshold);
        waterControl.setLeague(league);
        waterControl.setTeamA(teamA);
        waterControl.setTeamB(teamB);
        waterControlRepository.save(waterControl);
        return true;
    }

    /**
     * 电竞 - 指定盘口
     * @param open
     * @param threshold
     * @return
     */
    @RequestMapping("/dj_zd_control")
    public boolean djzdControl(@RequestParam String open, @RequestParam String threshold) {
        WaterControl waterControl = waterControlRepository.getOne(CrawlerTaskV2.DJ_ZD_CONTROL_ID);
        waterControl.setEnable(open);
        waterControl.setThreshold(threshold);
        waterControlRepository.save(waterControl);
        return true;
    }

    /**
     * 电竞 - 体育报水控制器
     * @param open
     * @param threshold
     * @return
     */
    @RequestMapping("/ty_control")
    public boolean tyControl(@RequestParam String open, @RequestParam String threshold) {
        WaterControl waterControl = waterControlRepository.getOne(CrawlerTaskV2.TY_CONTROL_ID);
        waterControl.setEnable(open);
        waterControl.setThreshold(threshold);
        waterControlRepository.save(waterControl);
        return true;
    }

    @RequestMapping("/ty_bp_control")
    public boolean tyBpControl(@RequestParam String open, @RequestParam String threshold, @RequestParam String league, @RequestParam String teamA, @RequestParam String teamB) {
        WaterControl waterControl = waterControlRepository.getOne(CrawlerTaskV2.TY_BP_CONTROL_ID);
        waterControl.setEnable(open);
        waterControl.setThreshold(threshold);
        waterControl.setLeague(league);
        waterControl.setTeamA(teamA);
        waterControl.setTeamB(teamB);
        waterControlRepository.save(waterControl);
        return true;
    }
}
