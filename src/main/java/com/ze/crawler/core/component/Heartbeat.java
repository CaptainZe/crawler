package com.ze.crawler.core.component;

import com.alibaba.fastjson.JSONObject;
import com.ze.crawler.core.constants.*;
import com.ze.crawler.core.model.RgESportsResultModel;
import com.ze.crawler.core.service.wk.WeiKongService;
import com.ze.crawler.core.utils.HttpClientUtils;
import com.ze.crawler.core.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 盘口心跳检测
 */
@SuppressWarnings("all")
@Slf4j
@Component
public class Heartbeat {
    @Autowired
    private WeiKongService weiKongService;

    /**
     * 电竞盘口心跳
     */
    @Scheduled(initialDelay = 1000 * 10, fixedDelay = 1000 * 60 * 60)
    public void heartbeat() {
        log.info("爬虫心跳检测开始");

        /* 电竞 */

        // 平博盘口
        pbHeartbeat();

        // RG盘口
        rgHeartbeat();

        // TF盘口
        tfHeartbeat();

        // IM盘口
        imHeartbeat();

        // 泛亚盘口
        fyHeartbeat();

        /* 体育 */

        // 平博
        pbHeartbeat4Sport();

        // IM
        imHeartbeat4Sport();

        // 188
        ybbHeartbeat4Sport();

        // BTI
        btiHeartbeat4Sport();

        log.info("爬虫心跳检测结束");
    }

    /* ====================== 平博电竞 - start  ====================== */
    private void pbHeartbeat() {
        int retryCount = 0;
        while (true) {
            String url = String.format(PBConstant.PB_BASE_URL, PBConstant.MK_TODAY, PBConstant.SP_ESPORTS, TimeUtils.getDate(), System.currentTimeMillis());
            Map<String, Object> map = HttpClientUtils.get(url, Map.class, ProxyConstant.DISH_USE_PROXY.get(Constant.ESPORTS_DISH_PB));
            if (map != null) {
                break;
            }

            retryCount++;
            if (retryCount >= Constant.RETRY_COUNT) {
                // 报警
                weiKongService.sendText("平博电竞异常", WKConstant.SEND_TYPE_ESPORTS_BP);
                break;
            }
        }
    }
    /* ====================== 平博电竞 - end  ====================== */

    /* ====================== RG电竞 - start  ====================== */
    private void rgHeartbeat() {
        int retryCount = 0;
        while (true) {
            String url = String.format(RGConstant.RG_BASE_URL, 0, RGConstant.MATCH_TYPE_TODAY);
            RgESportsResultModel rgESportsResultModel = HttpClientUtils.get(url, RgESportsResultModel.class);
            if (rgESportsResultModel != null) {
                break;
            }

            retryCount++;
            if (retryCount >= Constant.RETRY_COUNT) {
                // 报警
                weiKongService.sendText("RG电竞异常", WKConstant.SEND_TYPE_ESPORTS_BP);
                break;
            }
        }
    }
    /* ====================== RG电竞 - end  ====================== */

    /* ====================== TF电竞 - start  ====================== */
    // 认证token
    private final static String AUTHORIZATION = "Token c4b789e82ce341ac985e44b6b4da5042";

    private void tfHeartbeat() {
        int retryCount = 0;
        while (true) {
            String url = String.format(TFConstant.TF_TODAY_URL, TFConstant.GAME_ID_DOTA2);
            List tfESportsResultModels = HttpClientUtils.get(url, List.class, AUTHORIZATION);
            if (tfESportsResultModels != null) {
                break;
            }

            retryCount++;
            if (retryCount >= Constant.RETRY_COUNT) {
                // 报警
                weiKongService.sendText("TF电竞异常", WKConstant.SEND_TYPE_ESPORTS_BP);
                break;
            }
        }
    }
    /* ====================== TF电竞 - end  ====================== */

    /* ====================== IM电竞 - start  ====================== */
    private void imHeartbeat() {
        int retryCount = 0;
        while (true) {
            JSONObject body = getBaseBody(IMConstant.SPORT_ID_DOTA2);
            Map<String, Object> map = HttpClientUtils.post(IMConstant.IM_BASE_URL_V1, body, Map.class, ProxyConstant.DISH_USE_PROXY.get(Constant.ESPORTS_DISH_IM));
            if (map != null) {
                break;
            }

            retryCount++;
            if (retryCount >= Constant.RETRY_COUNT) {
                // 报警
                weiKongService.sendText("IM电竞异常", WKConstant.SEND_TYPE_ESPORTS_BP);
                break;
            }
        }
    }

    private JSONObject getBaseBody(Integer sportId) {
        JSONObject body = new JSONObject();
        body.put("BettingChannel", 1);
        body.put("EventMarket", -99);
        body.put("Language", "chs");
        body.put("Token", null);
        body.put("BaseLGIds", Collections.singletonList(-99));
        body.put("SportId", sportId);
        body.put("MatchCnt", 200);
        body.put("SortType", 1);
        return body;
    }
    /* ====================== IM电竞 - end  ====================== */

    /* ====================== 泛亚电竞 - start  ====================== */
    private void fyHeartbeat() {
        int retryCount = 0;
        while (true) {
            Map<String, String> headers = getRequestHeaders(FYConstant.PATH_MATCH_LIST);
            Map<String, Object> map = HttpClientUtils.postFrom(FYConstant.FY_BASE_URL, null, headers, Map.class, ProxyConstant.DISH_USE_PROXY.get(Constant.ESPORTS_DISH_FY));
            if (map != null) {
                break;
            }

            retryCount++;
            if (retryCount >= Constant.RETRY_COUNT) {
                // 报警
                weiKongService.sendText("泛亚电竞异常", WKConstant.SEND_TYPE_ESPORTS_BP);
                break;
            }
        }
    }

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
    /* ====================== 泛亚电竞 - end  ====================== */

    /* ====================== 平博体育 - start  ====================== */
    private void pbHeartbeat4Sport() {
        int retryCount = 0;
        while (true) {
            String url = String.format(PBConstant.PB_BASE_URL, PBConstant.MK_TODAY, PBConstant.SP_SOCCER, "", System.currentTimeMillis());
            Map<String, Object> map = HttpClientUtils.get(url, Map.class, ProxyConstant.DISH_USE_PROXY.get(Constant.SPORTS_DISH_PB));
            if (map != null) {
                break;
            }

            retryCount++;
            if (retryCount >= Constant.RETRY_COUNT) {
                // 报警
                weiKongService.sendText("平博体育异常", WKConstant.SEND_TYPE_ESPORTS_BP);
                break;
            }
        }
    }
    /* ====================== 平博体育 - end  ====================== */

    /* ====================== IM体育 - start  ====================== */
    private void imHeartbeat4Sport() {
        int retryCount = 0;
        while (true) {
            JSONObject todayBody = getBaseBody(IMConstant.SPORT_ID_SOCCER, IMConstant.MARKET_TODAY, null, null);
            Map<String, Object> map = HttpClientUtils.post(IMConstant.IM_SPORT_BASE_URL, todayBody, Map.class, ProxyConstant.DISH_USE_PROXY.get(Constant.SPORTS_DISH_IM));
            if (map != null) {
                break;
            }

            retryCount++;
            if (retryCount >= Constant.RETRY_COUNT) {
                // 报警
                weiKongService.sendText("IM体育异常", WKConstant.SEND_TYPE_ESPORTS_BP);
                break;
            }
        }
    }

    private JSONObject getBaseBody(Integer sportId, Integer market, String dateFrom, String dateTo) {
        JSONObject body = new JSONObject();
        body.put("BetTypeIds", new Integer[] {1, 2, 4});
        body.put("DateFrom", null);
        body.put("DateTo", null);
        if (!StringUtils.isEmpty(dateFrom) && !StringUtils.isEmpty(dateTo)) {
            body.put("DateFrom", dateFrom);
            body.put("DateTo", dateTo);
        }
        body.put("IsCombo", false);
        body.put("Market", market);
        body.put("MatchDay", 0);
        body.put("OddsType", 3);
        body.put("PeriodIds", new Integer[] {1, 2});
        body.put("Season", 0);
        body.put("SortType", 1);
        body.put("SportId", sportId);
        return body;
    }
    /* ====================== IM体育 - end  ====================== */

    /* ====================== 188体育 - start  ====================== */
    private void ybbHeartbeat4Sport() {
        int retryCount = 0;
        while (true) {
            String url = String.format(YBBConstant.YBB_BASE_URL, System.currentTimeMillis());
            Map<String, Object> map = HttpClientUtils.postFrom(url, getFormData(true, Constant.SPORTS_TYPE_SOCCER), Map.class);
            if (map != null) {
                break;
            }

            retryCount++;
            if (retryCount >= Constant.RETRY_COUNT) {
                // 报警
                weiKongService.sendText("188体育异常", WKConstant.SEND_TYPE_ESPORTS_BP);
                break;
            }
        }
    }

    private Map<String, Object> getFormData(boolean isToday, String type) {
        Map<String, Object> params = new HashMap<>();
        params.put("IsFirstLoad", true);
        params.put("VersionL", -1);
        params.put("VersionU", 0);
        params.put("VersionS", -1);
        params.put("VersionF", -1);
        params.put("VersionH", 0);
        params.put("VersionT", -1);
        params.put("IsEventMenu", false);
        params.put("SportID", 1);
        params.put("CompetitionID", -1);
        params.put("oIsInplayAll", false);
        params.put("oIsFirstLoad", true);
        params.put("oSortBy", 1);
        params.put("oOddsType", 0);
        params.put("oPageNo", 0);
        params.put("LiveCenterEventId", 0);
        params.put("LiveCenterSportId", 0);

        if (Constant.SPORTS_TYPE_SOCCER.equalsIgnoreCase(type)) {
            // 足球
            if (isToday) {
                params.put("reqUrl", "/zh-cn/sports/football/matches-by-date/today/full-time-asian-handicap-and-over-under");
                params.put("hisUrl", "/zh-cn/sports/football/matches-by-date/today/full-time-asian-handicap-and-over-under?q=&country=CN&currency=RMB&tzoff=-240&reg=China&rc=CN&allowRacing=false");
            } else {
                params.put("reqUrl", "/zh-cn/sports/football/matches-by-date/tomorrow/full-time-asian-handicap-and-over-under");
                params.put("hisUrl", "/zh-cn/sports/football/matches-by-date/today/full-time-asian-handicap-and-over-under");
            }
        } else {
            // 篮球
            params.put("reqUrl", "/zh-cn/sports/basketball/competition/full-time-asian-handicap-and-over-under");
            params.put("hisUrl", "/zh-cn/sports/basketball/competition/full-time-asian-handicap-and-over-under?q=&country=CN&currency=RMB&tzoff=-240&reg=China&rc=CN&allowRacing=false");
        }

        return params;
    }
    /* ====================== 188体育 - end  ====================== */

    /* ====================== BTI体育 - start  ====================== */
    private void btiHeartbeat4Sport() {
        int retryCount = 0;
        while (true) {
            String url = String.format(BTIConstant.BTI_TODAY_URL, BTIConstant.BRANCH_ID_SOCCER);
            List list = HttpClientUtils.get(url, List.class, getRequestHeaders(), null, false);
            if (list != null) {
                break;
            }

            retryCount++;
            if (retryCount >= Constant.RETRY_COUNT) {
                // 报警
                weiKongService.sendText("BTI体育异常", WKConstant.SEND_TYPE_ESPORTS_BP);
                break;
            }
        }
    }

    private Map<String, String> getRequestHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("RequestTarget", "AJAXService");
        return headers;
    }
    /* ====================== BTI体育 - end  ====================== */
}
