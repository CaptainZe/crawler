package com.ze.crawler.core.repository;

import com.ze.crawler.core.entity.ALog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRepository extends JpaRepository<ALog, String> {
}
