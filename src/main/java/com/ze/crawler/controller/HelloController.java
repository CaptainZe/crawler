package com.ze.crawler.controller;

import com.ze.crawler.core.constants.Constant;
import com.ze.crawler.core.model.TeamFilterModel;
import com.ze.crawler.core.service.*;
import com.ze.crawler.core.service.executor.ESportsExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

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
    private PbSportsService pbSportsService;
    @Autowired
    private ImSportsService imSportsService;
    @Autowired
    private YbbSportsService ybbSportsService;

    @RequestMapping("/index")
    public String index() {
        return "Hello World!!!";
    }

    @RequestMapping("/test")
    public void test() {
        TeamFilterModel teamFilterModel = new TeamFilterModel();
        teamFilterModel.setTeamOne("10106");
        teamFilterModel.setTeamTwo("10101");

        pbESportsService.crawler("110", Constant.ESPORTS_TYPE_KPL,null, null);
        rgESportsService.crawler("110", Constant.ESPORTS_TYPE_KPL,null, null);
        tfESportsService.crawler("110", Constant.ESPORTS_TYPE_KPL,null, null);
        imESportsService.crawler("110", Constant.ESPORTS_TYPE_KPL,null, null);
    }

    @RequestMapping("/sports")
    public void sports() {
        pbSportsService.crawler("110", Constant.SPORTS_TYPE_SOCCER,null, null);
        imSportsService.crawler("110", Constant.SPORTS_TYPE_SOCCER,null, null);
        ybbSportsService.crawler("110", Constant.SPORTS_TYPE_SOCCER,null, null);
    }

    @RequestMapping("/executor")
    public void executor() {
        TeamFilterModel teamFilterModel = new TeamFilterModel();
        teamFilterModel.setTeamOne("31707");
        teamFilterModel.setTeamTwo("31708");
        eSportsExecutor.executor("1", Constant.ESPORTS_TYPE_CSGO,
                Collections.singleton("317"), Collections.singletonList(teamFilterModel), -20, null);
    }
}
