package com.ze.crawler.core.service.log;

import com.alibaba.fastjson.JSON;
import com.ze.crawler.core.entity.ALog;
import com.ze.crawler.core.repository.LogRepository;
import com.ze.crawler.core.utils.LangUtils;
import com.ze.crawler.core.utils.TimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 日志
 */
@Service
public class LogService {

    @Autowired
    private LogRepository logRepository;

    /**
     * 日志记录
     */
    public void log(int type, String formDish, String data) {
        log(type, formDish, data, "");
    }

    /**
     * 日志记录
     */
    public void log(int type, String formDish, String data, Exception e) {
        String msg = "";
        if (e != null) {
            if (e.getLocalizedMessage() == null) {
                msg = JSON.toJSONString(e.getStackTrace());
            } else {
                msg = e.getLocalizedMessage();
            }
        }
        log(type, formDish, data, msg);
    }

    /**
     * 日志记录
     */
    public void log(int type, String formDish, String data, String msg) {
        ALog log = new ALog();
        log.setId(LangUtils.generateUuid());
        log.setType(type);
        log.setFromDish(formDish);
        log.setData(data);
        log.setMsg(msg);
        log.setCreateTime(TimeUtils.format(new Date().getTime()));
        logRepository.save(log);
    }
}
