package com.ze.crawler.core.entity;

import org.hibernate.annotations.Proxy;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Proxy(lazy = false)
@Table(name = "t_water_control", schema = "crawler")
public class WaterControl {
    private String id;
    private String enable;
    private String threshold;
    private String league;
    private String teamA;
    private String teamB;

    @Id
    @Column(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Basic
    @Column(name = "enable")
    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    @Basic
    @Column(name = "threshold")
    public String getThreshold() {
        return threshold;
    }

    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }

    @Basic
    @Column(name = "league")
    public String getLeague() {
        return league;
    }

    public void setLeague(String league) {
        this.league = league;
    }

    @Basic
    @Column(name = "team_a")
    public String getTeamA() {
        return teamA;
    }

    public void setTeamA(String teamA) {
        this.teamA = teamA;
    }

    @Basic
    @Column(name = "team_b")
    public String getTeamB() {
        return teamB;
    }

    public void setTeamB(String teamB) {
        this.teamB = teamB;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WaterControl that = (WaterControl) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(enable, that.enable) &&
                Objects.equals(threshold, that.threshold) &&
                Objects.equals(league, that.league) &&
                Objects.equals(teamA, that.teamA) &&
                Objects.equals(teamB, that.teamB);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, enable, threshold, league, teamA, teamB);
    }
}
