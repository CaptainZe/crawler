package com.ze.crawler.core.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TfESportsResultMarketSelectionModel {
    /**
     * 赔率 欧洲盘
     */
    private BigDecimal euroOdds;
    /**
     * 表示哪边, home or away
     */
    private String name;
    /**
     * 具体值
     */
    private String handicap;
    /**
     * 显示值
     */
    private String betTypeSelectionName;
    /**
     * 状态. open表示前台可见
     */
    private String status;
}
