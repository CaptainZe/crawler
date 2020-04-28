package com.ze.crawler.core.service;

/**
 * 电竞爬虫
 */
public interface ESportsService {

    /**
     * 爬虫
     * @param taskId
     * @param type
     */
    void crawler(String taskId, String type);
}
