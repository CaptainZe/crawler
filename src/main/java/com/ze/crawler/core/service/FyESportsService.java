package com.ze.crawler.core.service;

import com.alibaba.fastjson.JSON;
import com.ze.crawler.core.constants.Constant;
import com.ze.crawler.core.constants.Dictionary;
import com.ze.crawler.core.constants.FYConstant;
import com.ze.crawler.core.constants.ProxyConstant;
import com.ze.crawler.core.model.TeamFilterModel;
import com.ze.crawler.core.repository.FyEsportsRepository;
import com.ze.crawler.core.service.log.LogService;
import com.ze.crawler.core.utils.CommonUtils;
import com.ze.crawler.core.utils.FilterUtils;
import com.ze.crawler.core.utils.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 泛亚电竞盘口
 */
@SuppressWarnings("all")
@Slf4j
@Service
public class FyESportsService implements BaseService {
    @Autowired
    private FyEsportsRepository fyEsportsRepository;
    @Autowired
    private LogService logService;

    @Override
    public void crawler(String taskId, String type, Set<String> appointedLeagues, List<TeamFilterModel> appointedTeams) {
        log.info("泛亚电竞_" + type + "_" + taskId);
        long startTime = System.currentTimeMillis();

        int retryCount = 0;
        while (true) {
            Map<String, String> headers = getRequestHeaders(FYConstant.PATH_MATCH_LIST);
            Map<String, Object> map = HttpClientUtils.postFrom(FYConstant.FY_BASE_URL, null, headers, Map.class, ProxyConstant.USE_PROXY);
            if (!CollectionUtils.isEmpty(map)) {
                Map<String, Object> info = (Map<String, Object>) map.get("info");
                if (!CollectionUtils.isEmpty(info) && info.containsKey("Match")) {
                    List<Map<String, Object>> matchList = (List<Map<String, Object>>) info.get("Match");
                    try {
                        parseEsports(taskId, type, matchList, appointedLeagues, appointedTeams);
                    } catch (Exception e) {
                        Map<String, String> data = new HashMap<>();
                        data.put("url", FYConstant.FY_BASE_URL);
                        data.put("result", JSON.toJSONString(map));
                        data.put("retry_count", String.valueOf(retryCount));
                        logService.log(Constant.LOG_TYPE_PARSE_SPORTS_ERROR, Constant.SPORTS_DISH_YB.toString(), JSON.toJSONString(data), e);
                    }
                    break;
                }
            }

            retryCount++;
            if (retryCount >= Constant.RETRY_COUNT) {
                break;
            }
        }

        long endTime = System.currentTimeMillis();
        log.info("泛亚电竞_" + type + "_" + taskId + "_[耗时（秒）: " + CommonUtils.getSeconds(endTime - startTime) + "]");
    }

    /**
     * 解析电竞
     */
    private void parseEsports(String taskId, String type, List<Map<String, Object>> matchList, Set<String> appointedLeagues, List<TeamFilterModel> appointedTeams) {
        if (!CollectionUtils.isEmpty(matchList)) {
            // 遍历
            for (Map<String, Object> match : matchList) {
                // 比赛名
                String gameName = (String) match.get("GameName");

                // 根据type过滤想要的联赛
                if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_LOL)) {
                    if (!gameName.equalsIgnoreCase(FYConstant.GAME_NAME_LOL)) {
                        continue;
                    }
                } else if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_DOTA2)) {
                    if (!gameName.equalsIgnoreCase(FYConstant.GAME_NAME_DOTA2)) {
                        continue;
                    }
                } else if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_CSGO)) {
                    if (!gameName.equalsIgnoreCase(FYConstant.GAME_NAME_CSGO)) {
                        continue;
                    }
                } else if (type.equalsIgnoreCase(Constant.ESPORTS_TYPE_KPL)) {
                    if (!gameName.equalsIgnoreCase(FYConstant.GAME_NAME_KPL)) {
                        continue;
                    }
                } else {
                    // 其余赛事, 暂不需要
                    continue;
                }

                // 状态（只爬未开始的）
                String status = (String) match.get("Status");
                if (!FYConstant.MATCH_STATUS_BEGIN.equalsIgnoreCase(status)) {
                    continue;
                }

                // 联赛名
                String leagueName = (String) match.get("LeagueName");
                String leagueId = Dictionary.ESPORT_FY_LEAGUE_MAPPING.get(leagueName);
                if (leagueId == null) {
                    continue;
                }

                // 如果存在指定联赛, 进行过滤判断
                if (!FilterUtils.filterLeague(appointedLeagues, leagueId)) {
                    continue;
                }

                // 主客队信息
                String homeTeamName = ((String) match.get("HomeName"));
                String guestTeamName = ((String) match.get("AwayName"));
                if (StringUtils.isEmpty(homeTeamName) || StringUtils.isEmpty(guestTeamName)) {
                    continue;
                }

                homeTeamName = homeTeamName.trim();
                guestTeamName = guestTeamName.trim();

                // 获取主客队信息
                String homeTeamId = Dictionary.ESPORT_FY_LEAGUE_TEAM_MAPPING.get(leagueId).get(homeTeamName.toUpperCase());
                String guestTeamId = Dictionary.ESPORT_FY_LEAGUE_TEAM_MAPPING.get(leagueId).get(guestTeamName.toUpperCase());
                if (homeTeamId == null || guestTeamId == null) {
                    continue;
                }

                // 如果存在指定队伍, 进行过滤判断
                if (!FilterUtils.filterTeam(appointedTeams, homeTeamId, guestTeamId)) {
                    continue;
                }

                // 比赛ID
                Integer matchId = (Integer) match.get("ID");
                //
            }
        }
    }

    /**
     * 获取请求头
     */
    private Map<String, String> getRequestHeaders(String path) {
        Map<String, String> headers = new HashMap<>();
        headers.put("accept", "application/json, text/plain, */*");
        headers.put("accept-encoding", "gzip, deflate, br");
        headers.put("accept-language", "zh-CN,zh;q=0.9");
        headers.put("ghost", "60e1601dc3964090ac33e4d55ffe0bbe");
        headers.put("origin", "https://jingjib.aabv.top");
        headers.put("path", path);
        headers.put("referer", "https://jingjib.aabv.top/index.html?v=1.2.22");
        headers.put("x-forwarded-host", "jingjib.aabv.top");

        return headers;
    }
}
