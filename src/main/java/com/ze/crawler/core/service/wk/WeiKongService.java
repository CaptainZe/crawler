package com.ze.crawler.core.service.wk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ze.crawler.core.entity.Wk;
import com.ze.crawler.core.repository.WkRepository;
import com.ze.crawler.core.utils.HttpClientUtils;
import com.ze.crawler.core.utils.TimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import java.sql.Time;
import java.util.*;

/**
 * 微控
 */
@Service
public class WeiKongService {
    @Autowired
    private WkRepository wkRepository;

    // 微控域名
    private final static String DOMAIN = "http://xingshenapi.com/";
    // Authorization 登录一次永不失效
    private final static String AUTHORIZATION = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJwYXNzd29yZCI6ImUxMGFkYzM5NDliYTU5YWJiZTU2ZTA1N2YyMGY4ODNld1BDUDZvSVgyI0goMjVhcyIsImlzcyI6InhpbmdzaGVuZyIsImFjY291bnQiOiIxNTk1OTA0Njg5NCJ9.WA93YzyXsAcIjS0tvwPk0a886L_D20OpyLAWCqgo3x8";
    // 发送失败Code
    private final static String FAIL_CODE_1 = "10001";
    private final static String FAIL_CODE_2 = "1001";
    // 重试文本
    private final static String TRY_AGAIN = "请再试一次！";

    // 微控账号信息   Key: wId  Value:target
    public final static Map<String, String> WK_INFO = new HashMap<>();

    /**
     * 初始化
     */
    @PostConstruct
    void initWk() {
        List<Wk> wkList = wkRepository.findAll();
        if (!CollectionUtils.isEmpty(wkList)) {
            for (Wk wk : wkList) {
                WK_INFO.put(wk.getwId(), wk.getTargetWcId());
            }
        }
    }

    /**
     * 发送文本消息
     */
    @SuppressWarnings("all")
    public void sendText(String content, String at) {
        String api = "/sendText";
        String url = getUrl(api);
        JSONObject body = new JSONObject();
        String wId = getRandomKey();
        body.put("wId", wId);
        body.put("wcId", WK_INFO.get(wId));
        body.put("content", content);
        if (!StringUtils.isEmpty(at)) {
            body.put("at", at);
        }
        Map<String, Object> response = HttpClientUtils.post(url, body, Map.class, AUTHORIZATION);

        // 发送失败的情况
        String code = (String) response.get("code");
        if (code.equals(FAIL_CODE_1) || code.equals(FAIL_CODE_2)) {
            String anotherWid = getDifferentKey(wId);
            JSONObject anotherBody = new JSONObject();
            anotherBody.put("wId", anotherWid);
            anotherBody.put("wcId", WK_INFO.get(anotherWid));
            anotherBody.put("content", wId + "_发送失败");
            HttpClientUtils.post(url, anotherBody, Map.class, AUTHORIZATION);
        }
    }

    /**
     * 发送文本消息
     */
    public void sendText(String content) {
        sendText(content, null);
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
        body.put("wcId", WK_INFO.get(wId));
        body.put("content", content);
        Map<String, Object> response = HttpClientUtils.post(url, body, Map.class, AUTHORIZATION);
        return JSON.toJSONString(response);
    }

    @SuppressWarnings("all")
    public String reLoginAll() {
        // 1. 开发者账号退出微控平台（目的：使所有已登录的微信号下线）
        String logoutApi = "/member/logout";
        Map<String, Object> r1 = HttpClientUtils.post(getUrl(logoutApi), new JSONObject(), Map.class, AUTHORIZATION);
        if (r1.get("message").equals("失败")) {
            return TRY_AGAIN;
        }

        // 2. 登录开发者账号
        String loginApi = "/member/login";
        JSONObject loginBody = new JSONObject();
        loginBody.put("account", "15959046894");
        loginBody.put("password", "123456");
        Map<String, Object> r2 = HttpClientUtils.post(getUrl(loginApi), loginBody, Map.class);
        if (r2.get("message").equals("失败")) {
            return TRY_AGAIN;
        }

        // 3. 二次登录
        String secondLoginApi = "/secondLogin";
        List<Wk> list = wkRepository.findAll();
        if (!CollectionUtils.isEmpty(list)) {
            for (Wk wk : list) {
                JSONObject secondLoginBody = new JSONObject();
                secondLoginBody.put("wcId", wk.getWcId());
                secondLoginBody.put("type", 2);
                Map<String, Object> response = HttpClientUtils.post(getUrl(secondLoginApi), secondLoginBody, Map.class, AUTHORIZATION);
                if (response.get("message").equals("失败")) {
                    return TRY_AGAIN;
                }

                Map<String, Object> data = (Map<String, Object>) response.get("data");
                String wId = (String) data.get("wId");
                wk.setwId(wId);
                wk.setLoginTime(TimeUtils.format(new Date().getTime()));
                wkRepository.save(wk);
            }
        }

        // 4. 刷新缓存
        refreshWkInfo();

        return "成功！";
    }

    /**
     * 刷新
     */
    private void refreshWkInfo() {
        WK_INFO.clear();
        List<Wk> wkList = wkRepository.findAll();
        if (!CollectionUtils.isEmpty(wkList)) {
            for (Wk wk : wkList) {
                WK_INFO.put(wk.getwId(), wk.getTargetWcId());
            }
        }
    }

    /**
     * 获取URL
     */
    private static String getUrl(String api) {
        return DOMAIN + api;
    }

    /**
     * 随机获取WK_INFO中的一个key
     */
    private String getRandomKey() {
        Random random = new Random();
        int r = random.nextInt(WK_INFO.keySet().size());

        int i = 0;
        for (String key : WK_INFO.keySet()) {
            if (r == i) {
                return key;
            }
            i++;
        }
        return WK_INFO.keySet().iterator().next();
    }

    /**
     * 获取一个不一样的key
     */
    private String getDifferentKey(String key) {
        for (String k : WK_INFO.keySet()) {
            if (!k.equals(key)) {
                return k;
            }
        }
        return WK_INFO.keySet().iterator().next();
    }
}
