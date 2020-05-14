package com.ze.crawler.core.service.proxy;

import lombok.Data;

import java.util.List;

@Data
public class ProxyResult {

    private Integer code;
    private Boolean success;
    private String msg;
    private List<ProxyData> data;
}
