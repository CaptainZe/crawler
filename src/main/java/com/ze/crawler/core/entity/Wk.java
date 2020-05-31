package com.ze.crawler.core.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "t_wk", schema = "crawler")
public class Wk {
    private String wcId;
    private String wId;
    private String roomA;
    private String roomB;
    private String loginTime;
    private String enable;

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
    @Column(name = "room_a")
    public String getRoomA() {
        return roomA;
    }

    public void setRoomA(String roomA) {
        this.roomA = roomA;
    }

    @Basic
    @Column(name = "room_b")
    public String getRoomB() {
        return roomB;
    }

    public void setRoomB(String roomB) {
        this.roomB = roomB;
    }

    @Basic
    @Column(name = "login_time")
    public String getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }

    @Basic
    @Column(name = "enable")
    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wk wk = (Wk) o;
        return Objects.equals(wcId, wk.wcId) &&
                Objects.equals(wId, wk.wId) &&
                Objects.equals(roomA, wk.roomA) &&
                Objects.equals(roomB, wk.roomB) &&
                Objects.equals(loginTime, wk.loginTime) &&
                Objects.equals(enable, wk.enable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wcId, wId, roomA, roomB, loginTime, enable);
    }
}
