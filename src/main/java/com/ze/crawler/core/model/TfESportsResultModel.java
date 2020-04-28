package com.ze.crawler.core.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TfESportsResultModel {
    /**
     * 比赛ID, 查询具体局赔率时需要使用
     */
    private Integer eventId;
    /**
     * 联赛名
     */
    private String competitionName;
    /**
     * 用于判读是否滚球. true表示滚球或者有滚球, false表示正常
     */
    private String inPlay;
    /**
     * 轮数, 比如: BO5
     */
    private String bestOf;
    /**
     * 开始时间
     */
    private String startDatetime;
    /**
     * 主队信息
     */
    private TfESportsResultTeamModel home;
    /**
     * 客队信息
     */
    private TfESportsResultTeamModel away;
    /**
     * 各局信息
     */
    private List<TfESportsResultMarketTabModel> marketTabs;
    /**
     * 各局赔率信息
     */
    private List<TfESportsResultMarketModel> markets;
}
