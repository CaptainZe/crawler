package com.ze.crawler.core.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@MappedSuperclass
public class Sports implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String taskId;
    private String type;
    private String dishId;
    private String dishName;
    private String leagueId;
    private String leagueName;
    private String homeTeamId;
    private String homeTeamName;
    private String guestTeamId;
    private String guestTeamName;
    private String startTime;
    private String homeTeamOdds;
    private String guestTeamOdds;
    private String homeTeamItem;
    private String guestTeamItem;
    private String homeExtraDishName;
    private String guestExtraDishName;

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
    @Column(name = "start_time")
    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    @Basic
    @Column(name = "home_team_odds")
    public String getHomeTeamOdds() {
        return homeTeamOdds;
    }

    public void setHomeTeamOdds(String homeTeamOdds) {
        this.homeTeamOdds = homeTeamOdds;
    }

    @Basic
    @Column(name = "guest_team_odds")
    public String getGuestTeamOdds() {
        return guestTeamOdds;
    }

    public void setGuestTeamOdds(String guestTeamOdds) {
        this.guestTeamOdds = guestTeamOdds;
    }

    @Basic
    @Column(name = "home_team_item")
    public String getHomeTeamItem() {
        return homeTeamItem;
    }

    public void setHomeTeamItem(String homeTeamItem) {
        this.homeTeamItem = homeTeamItem;
    }

    @Basic
    @Column(name = "guest_team_item")
    public String getGuestTeamItem() {
        return guestTeamItem;
    }

    public void setGuestTeamItem(String guestTeamItem) {
        this.guestTeamItem = guestTeamItem;
    }

    @Basic
    @Column(name = "home_extra_dish_name")
    public String getHomeExtraDishName() {
        return homeExtraDishName;
    }

    public void setHomeExtraDishName(String homeExtraDishName) {
        this.homeExtraDishName = homeExtraDishName;
    }

    @Basic
    @Column(name = "guest_extra_dish_name")
    public String getGuestExtraDishName() {
        return guestExtraDishName;
    }

    public void setGuestExtraDishName(String guestExtraDishName) {
        this.guestExtraDishName = guestExtraDishName;
    }
}
