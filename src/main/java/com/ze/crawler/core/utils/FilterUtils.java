package com.ze.crawler.core.utils;

import com.ze.crawler.core.model.TeamFilterModel;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

/**
 * 过滤工具类
 */
public class FilterUtils {

    /**
     * 过滤联赛
     * @param appointedLeagues
     * @param leagueId
     * @return
     */
    public static boolean filterLeague(Set<String> appointedLeagues, String leagueId) {
        if (CollectionUtils.isEmpty(appointedLeagues)) {
            return true;
        }

        return appointedLeagues.contains(leagueId);
    }

    /**
     * 过滤比赛队伍
     * @param appointedTeams
     * @param homeTeamId
     * @param guestTeamId
     * @return
     */
    public static boolean filterTeam(List<TeamFilterModel> appointedTeams, String homeTeamId, String guestTeamId) {
        if (CollectionUtils.isEmpty(appointedTeams)) {
            return true;
        }

        for (TeamFilterModel teamFilterModel : appointedTeams) {
            if (teamFilterModel.getTeamOne().equals(homeTeamId) && teamFilterModel.getTeamTwo().equals(guestTeamId)) {
                return true;
            }
            if (teamFilterModel.getTeamTwo().equals(homeTeamId) && teamFilterModel.getTeamOne().equals(guestTeamId)) {
                return true;
            }
        }

        return false;
    }
}
