package com.ze.crawler.core.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RgESportsResultItemOddsModel {
    /**
     * 状态 为1才有意义
     */
    private Integer status;
    /**
     * 盘口名, 比如: 获胜者
     */
    private String groupName;
    /**
     * 比赛局, 比如: final为全场
     */
    private String matchStage;
    /**
     * 代表多种意义
     */
    private String name;
    /**
     * 赔率
     */
    private String odds;
    /**
     * 赔率组。 通过这个判断是对手盘
     */
    private Integer oddsGroupId;
    /**
     * 同一组,oddsId不同
     */
    private Integer oddsId;
    /**
     * 代表多种意义
     */
    private String value;
    /**
     * 队伍ID
     */
    private Integer teamId;
}
