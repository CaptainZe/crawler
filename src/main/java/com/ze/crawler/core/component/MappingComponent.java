package com.ze.crawler.core.component;

import com.ze.crawler.core.service.mapping.MappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class MappingComponent {

    @Autowired
    private MappingService mappingService;

    @PostConstruct
    void initMapping() {
        // 电竞Mapping初始化
        mappingService.esportsMapping();
        log.info("电竞Mapping初始化成功");

        // 体育Mapping初始化
        mappingService.sportsMapping();
        log.info("体育Mapping初始化成功");
    }
}
