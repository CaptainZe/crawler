package com.ze.crawler.core.service.wk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ze.crawler.core.constants.WkConstant;
import com.ze.crawler.core.entity.Wk;
import com.ze.crawler.core.repository.WkRepository;
import com.ze.crawler.core.utils.HttpClientUtils;
import com.ze.crawler.core.utils.TimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * 微控
 */
@Service
public class WeiKongService {
    @Autowired
    private WkRepository wkRepository;

    /**
     * 初始化
     */
    @PostConstruct
    void initWk() {
        initAndRefreshWkInfo();
    }

    /**
     * 发送文本消息
     */
    public void sendText(String content, String at, Integer sendType) {
        String api = "/sendText";
        String url = getUrl(api);
        JSONObject body = getRandomAccount(sendType);
        body.put("content", content);
        if (!StringUtils.isEmpty(at)) {
            body.put("at", at);
        }
        HttpClientUtils.post(url, body, Map.class, WkConstant.AUTHORIZATION);
    }

    /**
     * 发送文本消息
     */
    public void sendText(String content, Integer sendType) {
        sendText(content, null, sendType);
    }

    /**
     * 测试各个账号发送情况
     * @param wId
     * @param content
     */
    @SuppressWarnings("all")
    public String sendTextByWid(String wId, String content) {
        String api = "/sendText";
        String url = getUrl(api);
        JSONObject body = new JSONObject();
        body.put("wId", wId);
        body.put("wcId", WkConstant.WK_ESPORTS_BP.get(wId));
        body.put("content", content);
        Map<String, Object> response = HttpClientUtils.post(url, body, Map.class, WkConstant.AUTHORIZATION);
        return JSON.toJSONString(response);
    }

    /**
     * 重新登录全部账号
     */
    @SuppressWarnings("all")
    public String reLoginAll() {
        // 1. 开发者账号退出微控平台（目的：使所有已登录的微信号下线）
        String logoutApi = "/member/logout";
        Map<String, Object> r1 = HttpClientUtils.post(getUrl(logoutApi), new JSONObject(), Map.class, WkConstant.AUTHORIZATION);
        if (r1.get("message").equals("失败")) {
            return WkConstant.TRY_AGAIN;
        }

        // 2. 登录开发者账号
        String loginApi = "/member/login";
        JSONObject loginBody = new JSONObject();
        loginBody.put("account", "15959046894");
        loginBody.put("password", "123456");
        Map<String, Object> r2 = HttpClientUtils.post(getUrl(loginApi), loginBody, Map.class);
        if (r2.get("message").equals("失败")) {
            return WkConstant.TRY_AGAIN;
        }

        // 3. 二次登录
        String secondLoginApi = "/secondLogin";
        List<Wk> list = wkRepository.findAll();
        if (!CollectionUtils.isEmpty(list)) {
            for (Wk wk : list) {
                JSONObject secondLoginBody = new JSONObject();
                secondLoginBody.put("wcId", wk.getWcId());
                secondLoginBody.put("type", 2);
                Map<String, Object> response = HttpClientUtils.post(getUrl(secondLoginApi), secondLoginBody, Map.class, WkConstant.AUTHORIZATION);
                if (response.get("message").equals("失败")) {
                    return WkConstant.TRY_AGAIN;
                }
                if (response.get("message").equals("二次登录失败，请重新扫码登录")) {
                    return "二次登录失败，请重新扫码登录";
                }

                Map<String, Object> data = (Map<String, Object>) response.get("data");
                String wId = (String) data.get("wId");
                wk.setwId(wId);
                wk.setLoginTime(TimeUtils.format(new Date().getTime()));
                wkRepository.save(wk);
            }
        }

        // 4. 刷新缓存
        initAndRefreshWkInfo();

        return "成功！";
    }

    /**
     * 刷新&初始化
     */
    private void initAndRefreshWkInfo() {
        WkConstant.WK_ESPORTS_YL.clear();
        WkConstant.WK_ESPORTS_BP.clear();
        List<Wk> wkList = wkRepository.findAll();
        if (!CollectionUtils.isEmpty(wkList)) {
            for (Wk wk : wkList) {
                WkConstant.WK_ESPORTS_YL.put(wk.getwId(), wk.getRoomA());
                WkConstant.WK_ESPORTS_BP.put(wk.getwId(), wk.getRoomB());
            }
        }
    }

    /**
     * 随机获取一个账号进行发送
     */
    private JSONObject getRandomAccount(Integer sendType) {
        JSONObject jsonObject = new JSONObject();

        Map<String, String> wkInfo = WkConstant.WK_ESPORTS_YL;
        // fixme 扩展点
        if (sendType.equals(WkConstant.ESPORTS_BP)) {
            wkInfo = WkConstant.WK_ESPORTS_BP;
        }

        Random random = new Random();
        int r = random.nextInt(wkInfo.keySet().size());

        int i = 0;
        for (String key : wkInfo.keySet()) {
            if (r == i) {
                jsonObject.put("wId", key);
                jsonObject.put("wcId", wkInfo.get(key));
                return jsonObject;
            }
            i++;
        }

        String wId = wkInfo.keySet().iterator().next();
        jsonObject.put("wId", wId);
        jsonObject.put("wcId", wkInfo.get(wId));
        return jsonObject;
    }

    /**
     * 获取URL
     */
    private static String getUrl(String api) {
        return WkConstant.DOMAIN + api;
    }
}
