package com.ze.crawler.core.service;

import com.ze.crawler.core.model.TeamFilterModel;

import java.util.List;
import java.util.Set;

/**
 * 爬虫基础类
 */
public interface BaseService {

    /**
     * 爬虫
     * @param taskId
     * @param type
     * @param appointedLeagues  指定联赛
     * @param appointedTeams    指定队伍
     */
    void crawler(String taskId, String type, Set<String> appointedLeagues, List<TeamFilterModel> appointedTeams);
}
