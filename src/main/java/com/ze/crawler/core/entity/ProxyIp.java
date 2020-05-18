package com.ze.crawler.core.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "t_proxy_ip", schema = "crawler")
public class ProxyIp {
    private String id;
    private String ip;
    private Integer port;
    private String expireTime;
    private String city;
    private String isp;
    private String scene;
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
    @Column(name = "ip")
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Basic
    @Column(name = "port")
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Basic
    @Column(name = "expire_time")
    public String getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(String expireTime) {
        this.expireTime = expireTime;
    }

    @Basic
    @Column(name = "city")
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Basic
    @Column(name = "isp")
    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    @Basic
    @Column(name = "scene")
    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
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
        ProxyIp proxyIp = (ProxyIp) o;
        return Objects.equals(id, proxyIp.id) &&
                Objects.equals(ip, proxyIp.ip) &&
                Objects.equals(port, proxyIp.port) &&
                Objects.equals(expireTime, proxyIp.expireTime) &&
                Objects.equals(city, proxyIp.city) &&
                Objects.equals(isp, proxyIp.isp) &&
                Objects.equals(scene, proxyIp.scene) &&
                Objects.equals(createTime, proxyIp.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ip, port, expireTime, city, isp, scene, createTime);
    }
}
