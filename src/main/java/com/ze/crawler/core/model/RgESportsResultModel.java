package com.ze.crawler.core.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RgESportsResultModel {
    /**
     * 结果
     */
    List<RgESportsResultItemModel> result;
}
