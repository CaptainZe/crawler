package com.ze.crawler.controller;

import com.ze.crawler.core.service.mapping.MappingSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mapping")
public class MappingController {

    @Autowired
    private MappingSupportService mappingSupportService;

    @RequestMapping("/support/esport")
    public void mappingSupport() {
        mappingSupportService.mappingSupport();
    }

    @RequestMapping("/support/sport")
    public void mappingSupport4Sport() {
        mappingSupportService.mappingSupport4Sport();
    }
}
