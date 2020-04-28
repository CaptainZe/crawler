package com.ze.crawler.core.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "t_wk", schema = "crawler")
public class Wk {
    private String wId;
    private String wcId;
    private String wcName;
    private String targetWcId;
    private String loginTime;

    @Id
    @Column(name = "wc_id")
    public String getWcId() {
        return wcId;
    }

    public void setWcId(String wcId) {
        this.wcId = wcId;
    }

    @Basic
    @Column(name = "w_id")
    public String getwId() {
        return wId;
    }

    public void setwId(String wId) {
        this.wId = wId;
    }

    @Basic
    @Column(name = "wc_name")
    public String getWcName() {
        return wcName;
    }

    public void setWcName(String wcName) {
        this.wcName = wcName;
    }

    @Basic
    @Column(name = "target_wc_id")
    public String getTargetWcId() {
        return targetWcId;
    }

    public void setTargetWcId(String targetWcId) {
        this.targetWcId = targetWcId;
    }

    @Basic
    @Column(name = "login_time")
    public String getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wk wk = (Wk) o;
        return Objects.equals(wId, wk.wId) &&
                Objects.equals(wcId, wk.wcId) &&
                Objects.equals(wcName, wk.wcName) &&
                Objects.equals(targetWcId, wk.targetWcId) &&
                Objects.equals(loginTime, wk.loginTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wId, wcId, wcName, targetWcId, loginTime);
    }
}