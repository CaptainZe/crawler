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

    // 微控账号信息   Key: wId  Value:target
    public final static Map<String, String> WK_INFO = new HashMap<>();

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
     * 刷新
     */
    public void refreshWkInfo() {
        List<Wk> wkList = wkRepository.findAll();
        if (!CollectionUtils.isEmpty(wkList)) {
            for (Wk wk : wkList) {
                WK_INFO.put(wk.getwId(), wk.getTargetWcId());
            }
        }
    }

    /**
     * 添加微控信息
     */
    public void addWk(String wId, String wcId, String nickName, String targetWcId) {
        Wk wk = new Wk();
        wk.setWcId(wcId);
        wk.setwId(wId);
        wk.setWcName(nickName);
        wk.setTargetWcId(targetWcId);
        wk.setLoginTime(TimeUtils.format(new Date().getTime()));
        wkRepository.save(wk);
    }

    /**
     * 更新微控信息
     */
    public void updateWk(String wId, String wcId, String nickName, String targetWcId) {
        Wk wk = wkRepository.getOne(wcId);;
        wk.setWcId(wcId);
        wk.setwId(wId);
        wk.setWcName(nickName);
        wk.setTargetWcId(targetWcId);
        wk.setLoginTime(TimeUtils.format(new Date().getTime()));
        wkRepository.save(wk);
    }
}
