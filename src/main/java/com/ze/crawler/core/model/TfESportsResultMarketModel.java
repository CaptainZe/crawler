package com.ze.crawler.core.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TfESportsResultMarketModel {
    /**
     * 盘口名
     */
    private String marketName;
    /**
     * 赔率信息
     */
    private List<TfESportsResultMarketSelectionModel> selection;
}
