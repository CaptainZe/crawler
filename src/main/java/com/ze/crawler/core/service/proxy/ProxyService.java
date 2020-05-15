package com.ze.crawler.core.service.proxy;

import com.ze.crawler.core.constants.Constant;
import com.ze.crawler.core.constants.ProxyConstant;
import com.ze.crawler.core.entity.ProxyIp;
import com.ze.crawler.core.repository.ProxyIpRepository;
import com.ze.crawler.core.utils.HttpClientUtils;
import com.ze.crawler.core.utils.LangUtils;
import com.ze.crawler.core.utils.TimeUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;

/**
 * 代理IP
 */
@Service
public class ProxyService {
    @Autowired
    private ProxyIpRepository proxyIpRepository;

    // 芝麻IP
    private final static String GET_PROXY_IP_URL = "http://webapi.http.zhimacangku.com/getip?num=1&type=2&pro=0&city=0&yys=0&port=11&time=4&ts=1&ys=1&cs=1&lb=1&sb=0&pb=4&mr=1&regions=";

    /**
     * 获取代理IP
     */
    public void getProxyIp() {
        int retryCount = 0;
        while (true) {
            ProxyResult result = HttpClientUtils.get(GET_PROXY_IP_URL, ProxyResult.class);
            if (result.getSuccess()) {
                if (!CollectionUtils.isEmpty(result.getData())) {
                    ProxyData proxyData = result.getData().get(0);

                    ProxyConstant.PROXY_HOST = proxyData.getIp();
                    ProxyConstant.PROXY_PORT = proxyData.getPort();

                    ProxyIp proxyIp = new ProxyIp();
                    BeanUtils.copyProperties(proxyData, proxyIp);
                    proxyIp.setId(LangUtils.generateUuid());
                    proxyIp.setCreateTime(TimeUtils.format(new Date().getTime()));
                    proxyIpRepository.save(proxyIp);
                    break;
                }
            }

            retryCount++;
            if (retryCount >= Constant.RETRY_COUNT) {
                break;
            }
        }
    }
}
