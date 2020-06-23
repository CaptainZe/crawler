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
    public void eSportsHeartbeat() {
        log.info("电竞心跳检测开始");

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

        log.info("电竞心跳检测结束");
    }

    /* ====================== 平博电竞 - start  ====================== */
    private void pbHeartbeat() {
        int retryCount = 0;
        while (true) {
            String url = String.format(PBConstant.PB_BASE_URL, PBConstant.MK_TODAY, PBConstant.SP_ESPORTS, TimeUtils.getDate(), System.currentTimeMillis());
            Map<String, Object> map = HttpClientUtils.get(url, Map.class);
            if (map != null) {
                break;
            }

            retryCount++;
            if (retryCount >= Constant.RETRY_COUNT) {
                // 报警
                weiKongService.sendText("平博电竞异常", WKConstant.SEND_TYPE_ESPORTS);
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
                weiKongService.sendText("RG电竞异常", WKConstant.SEND_TYPE_ESPORTS);
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
                weiKongService.sendText("TF电竞异常", WKConstant.SEND_TYPE_ESPORTS);
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
            Map<String, Object> map = HttpClientUtils.post(IMConstant.IM_BASE_URL_V1, body, Map.class, ProxyConstant.USE_PROXY);
            if (map != null) {
                break;
            }

            retryCount++;
            if (retryCount >= Constant.RETRY_COUNT) {
                // 报警
                weiKongService.sendText("IM电竞异常", WKConstant.SEND_TYPE_ESPORTS);
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
        return body;
    }
    /* ====================== IM电竞 - end  ====================== */

    /* ====================== 泛亚电竞 - start  ====================== */
    private void fyHeartbeat() {
        int retryCount = 0;
        while (true) {
            Map<String, String> headers = getRequestHeaders(FYConstant.PATH_MATCH_LIST);
            Map<String, Object> map = HttpClientUtils.postFrom(FYConstant.FY_BASE_URL, null, headers, Map.class, ProxyConstant.USE_PROXY);
            if (map != null) {
                break;
            }

            retryCount++;
            if (retryCount >= Constant.RETRY_COUNT) {
                // 报警
                weiKongService.sendText("泛亚电竞异常", WKConstant.SEND_TYPE_ESPORTS);
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
}
