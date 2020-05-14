package com.ze.crawler.core.service.proxy;

import lombok.Data;

@Data
public class ProxyData {
    private String ip;
    private Integer port;
    private String expireTime;
    private String city;
    private String isp;
}
