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
    private ImESportsServiceV1 imESportsServicev1;
    @Autowired
    private FyESportsService fyESportsService;
    @Autowired
    private ESportsExecutor eSportsExecutor;

    @Autowired
    private PbSportsService pbSportsService;
    @Autowired
    private ImSportsService imSportsService;
    @Autowired
    private YbbSportsService ybbSportsService;
    @Autowired
    private BtiSportService btiSportService;

    @RequestMapping("/index")
    public String index() {
        return "Hello World!!!";
    }

    @RequestMapping("/test")
    public void test() {
        TeamFilterModel teamFilterModel = new TeamFilterModel();
        teamFilterModel.setTeamOne("10105");
        teamFilterModel.setTeamTwo("10114");

        pbESportsService.crawler("110", Constant.ESPORTS_TYPE_DOTA2,null, null);
        rgESportsService.crawler("110", Constant.ESPORTS_TYPE_DOTA2,null, null);
        tfESportsService.crawler("110", Constant.ESPORTS_TYPE_DOTA2,null, null);
        imESportsServicev1.crawler("110", Constant.ESPORTS_TYPE_LOL,null, null);
        fyESportsService.crawler("110", Constant.ESPORTS_TYPE_DOTA2,null, null);
    }

    @RequestMapping("/sports")
    public void sports() {
//        pbSportsService.crawler("110", Constant.SPORTS_TYPE_SOCCER,null, null);
//        imSportsService.crawler("110", Constant.SPORTS_TYPE_SOCCER,null, null);
//        ybbSportsService.crawler("110", Constant.SPORTS_TYPE_SOCCER,null, null);
        btiSportService.crawler("111", Constant.SPORTS_TYPE_SOCCER,null, null);
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
