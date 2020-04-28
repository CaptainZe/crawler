package com.ze.crawler.core.entity;

import org.hibernate.annotations.Proxy;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Proxy(lazy = false)
@Table(name = "t_log", schema = "crawler")
public class ALog {
    private String id;
    private Integer type;
    private String fromDish;
    private String data;
    private String msg;
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
    @Column(name = "type")
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Basic
    @Column(name = "from_dish")
    public String getFromDish() {
        return fromDish;
    }

    public void setFromDish(String fromDish) {
        this.fromDish = fromDish;
    }

    @Basic
    @Column(name = "data")
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Basic
    @Column(name = "msg")
    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
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
        ALog aLog = (ALog) o;
        return Objects.equals(id, aLog.id) &&
                Objects.equals(type, aLog.type) &&
                Objects.equals(fromDish, aLog.fromDish) &&
                Objects.equals(data, aLog.data) &&
                Objects.equals(msg, aLog.msg) &&
                Objects.equals(createTime, aLog.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, fromDish, data, msg, createTime);
    }
}
