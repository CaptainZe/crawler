package com.ze.crawler.core.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "t_water_yield", schema = "crawler")
public class WaterYield {
    private String id;
    private String taskId;
    private String type;
    private Integer mainDish;
    private Integer rpDish;
    private String dishId;
    private String dishName;
    private String leagueId;
    private String leagueName;
    private String homeTeamId;
    private String homeTeamName;
    private String guestTeamId;
    private String guestTeamName;
    private String contrastInfo;
    private String display;
    private Double water;
    private String createTime;

    @Id
    @Column(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Basic
    @Column(name = "task_id")
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Basic
    @Column(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Basic
    @Column(name = "main_dish")
    public Integer getMainDish() {
        return mainDish;
    }

    public void setMainDish(Integer mainDish) {
        this.mainDish = mainDish;
    }

    @Basic
    @Column(name = "rp_dish")
    public Integer getRpDish() {
        return rpDish;
    }

    public void setRpDish(Integer rpDish) {
        this.rpDish = rpDish;
    }

    @Basic
    @Column(name = "dish_id")
    public String getDishId() {
        return dishId;
    }

    public void setDishId(String dishId) {
        this.dishId = dishId;
    }

    @Basic
    @Column(name = "dish_name")
    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    @Basic
    @Column(name = "league_id")
    public String getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(String leagueId) {
        this.leagueId = leagueId;
    }

    @Basic
    @Column(name = "league_name")
    public String getLeagueName() {
        return leagueName;
    }

    public void setLeagueName(String leagueName) {
        this.leagueName = leagueName;
    }

    @Basic
    @Column(name = "home_team_id")
    public String getHomeTeamId() {
        return homeTeamId;
    }

    public void setHomeTeamId(String homeTeamId) {
        this.homeTeamId = homeTeamId;
    }

    @Basic
    @Column(name = "home_team_name")
    public String getHomeTeamName() {
        return homeTeamName;
    }

    public void setHomeTeamName(String homeTeamName) {
        this.homeTeamName = homeTeamName;
    }

    @Basic
    @Column(name = "guest_team_id")
    public String getGuestTeamId() {
        return guestTeamId;
    }

    public void setGuestTeamId(String guestTeamId) {
        this.guestTeamId = guestTeamId;
    }

    @Basic
    @Column(name = "guest_team_name")
    public String getGuestTeamName() {
        return guestTeamName;
    }

    public void setGuestTeamName(String guestTeamName) {
        this.guestTeamName = guestTeamName;
    }

    @Basic
    @Column(name = "contrast_info")
    public String getContrastInfo() {
        return contrastInfo;
    }

    public void setContrastInfo(String contrastInfo) {
        this.contrastInfo = contrastInfo;
    }

    @Basic
    @Column(name = "display")
    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    @Basic
    @Column(name = "water")
    public Double getWater() {
        return water;
    }

    public void setWater(Double water) {
        this.water = water;
    }

    @Basic
    @Column(name = "create_time")
    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WaterYield that = (WaterYield) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(taskId, that.taskId) &&
                Objects.equals(type, that.type) &&
                Objects.equals(mainDish, that.mainDish) &&
                Objects.equals(rpDish, that.rpDish) &&
                Objects.equals(dishId, that.dishId) &&
                Objects.equals(dishName, that.dishName) &&
                Objects.equals(leagueId, that.leagueId) &&
                Objects.equals(leagueName, that.leagueName) &&
                Objects.equals(homeTeamId, that.homeTeamId) &&
                Objects.equals(homeTeamName, that.homeTeamName) &&
                Objects.equals(guestTeamId, that.guestTeamId) &&
                Objects.equals(guestTeamName, that.guestTeamName) &&
                Objects.equals(contrastInfo, that.contrastInfo) &&
                Objects.equals(display, that.display) &&
                Objects.equals(water, that.water) &&
                Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, taskId, type, mainDish, rpDish, dishId, dishName, leagueId, leagueName, homeTeamId, homeTeamName, guestTeamId, guestTeamName, contrastInfo, display, water, createTime);
    }
}
