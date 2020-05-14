package com.ze.crawler.core.repository;

import com.ze.crawler.core.entity.ProxyIp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProxyIpRepository extends JpaRepository<ProxyIp, String> {
}
