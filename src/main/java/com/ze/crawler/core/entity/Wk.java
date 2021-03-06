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
    private String roomC;
    private String roomD;
    private String roomE;
    private Integer usageScene;
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
    @Column(name = "room_c")
    public String getRoomC() {
        return roomC;
    }

    public void setRoomC(String roomC) {
        this.roomC = roomC;
    }

    @Basic
    @Column(name = "room_d")
    public String getRoomD() {
        return roomD;
    }

    public void setRoomD(String roomD) {
        this.roomD = roomD;
    }

    @Basic
    @Column(name = "room_e")
    public String getRoomE() {
        return roomE;
    }

    public void setRoomE(String roomE) {
        this.roomE = roomE;
    }

    @Basic
    @Column(name = "usage_scene")
    public Integer getUsageScene() {
        return usageScene;
    }

    public void setUsageScene(Integer usageScene) {
        this.usageScene = usageScene;
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
                Objects.equals(roomC, wk.roomC) &&
                Objects.equals(usageScene, wk.usageScene) &&
                Objects.equals(loginTime, wk.loginTime) &&
                Objects.equals(enable, wk.enable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wcId, wId, roomA, roomB, roomC, usageScene, loginTime, enable);
    }
}
