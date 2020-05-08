package com.ze.crawler.controller;

import com.ze.crawler.core.service.mapping.MappingService;
import com.ze.crawler.core.service.mapping.MappingSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mapping")
public class MappingController {

    @Autowired
    private MappingService mappingService;
    @Autowired
    private MappingSupportService mappingSupportService;

    /**
     * 更新电竞字典表
     * @return
     */
    @RequestMapping("/refresh_esports")
    public String refreshEsportsMapping() {
        return mappingService.esportsMapping();
    }

    @RequestMapping("/support")
    public void mappingSupport() {
        mappingSupportService.mappingSupport();
    }
}
