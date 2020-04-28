package com.ze.crawler.core.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RgESportsResultItemModel {
    /**
     * 状态 比如: 1表示未开始 2表示滚球中
     */
    private Integer status;
    /**
     * match_id, 查询详细盘口时使用
     */
    private Integer id;
    /**
     * 游戏名，比如: DOTA2
     */
    private String gameName;
    /**
     * 联赛名
     */
    private String tournamentName;
    /**
     * 开赛时间，比如: 2020-04-05 14:05:00
     */
    private String startTime;
    /**
     * 局数，比如: bo5
     */
    private String round;
    /**
     * 具体盘数据
     */
    private List<RgESportsResultItemOddsModel> odds;
    /**
     * 队伍信息 0是队伍2 1是队伍1
     */
    private List<RgESportsResultTeamModel> team;
}
