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
        TeamFilterModel teamFilterModel = new TeamFilterModel();
        teamFilterModel.setTeamOne("31707");
        teamFilterModel.setTeamTwo("31708");
        eSportsExecutor.executor("1", Constant.ESPORTS_TYPE_CSGO,
                Collections.singleton("317"), Collections.singletonList(teamFilterModel), -20, null);
    }
}
