package com.ze.crawler.controller;

import com.ze.crawler.core.constants.Constant;
import com.ze.crawler.core.model.TeamFilterModel;
import com.ze.crawler.core.service.ImESportsService;
import com.ze.crawler.core.service.PbESportsService;
import com.ze.crawler.core.service.RgESportsService;
import com.ze.crawler.core.service.TfESportsService;
import com.ze.crawler.core.service.executor.ESportsExecutor;
import com.ze.crawler.core.service.wk.WeiKongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("hello")
public class HelloController {

    @Autowired
    private PbESportsService pbESportsService;
    @Autowired
    private RgESportsService rgESportsService;
    @Autowired
    private TfESportsService tfESportsService;
    @Autowired
    private ImESportsService imESportsService;
    @Autowired
    private ESportsExecutor eSportsExecutor;
    @Autowired
    private WeiKongService weiKongService;

    @RequestMapping("/index")
    public String index() {
        return "Hello World!!!";
    }

    @RequestMapping("/test")
    public void test() {
        TeamFilterModel teamFilterModel = new TeamFilterModel();
        teamFilterModel.setTeamOne("10106");
        teamFilterModel.setTeamTwo("10101");

        pbESportsService.crawler("110", Constant.ESPORTS_TYPE_LOL, Collections.singleton("101"), Collections.singletonList(teamFilterModel));
    }

    @RequestMapping("/executor")
    public void executor() {
        eSportsExecutor.executor("1", Constant.ESPORTS_TYPE_LOL, null, null, -200, null);
    }

    @RequestMapping("/inform")
    public void inform() {
        weiKongService.sendText("test....");
        weiKongService.sendText("中文测试....");
        weiKongService.sendText("类型：LOL_比赛_让分盘\n" +
                " 平博电竞：英雄联盟 - 中国LPL\n" +
                " 平博电竞：Edward Gaming VS Royal Never Give Up\n" +
                " RG电竞：LPL春季赛季后赛\n" +
                " RG电竞：EDG VS RNG\n" +
                " 平博电竞开赛时间：2020-04-23 17:00:00\n" +
                " RG电竞开赛时间：2020-04-23 17:00:00\n" +
                " 平博电竞：比赛_让分盘_[Edward Gaming]_2.5_(1.098)\n" +
                " RG电竞：全场_地图让分_[RNG]_-2.5_(5.65)\n" +
                " 水量：-9.63");
    }
}
