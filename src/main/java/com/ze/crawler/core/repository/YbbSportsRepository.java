package com.ze.crawler.core.repository;

import com.ze.crawler.core.entity.YbbSports;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface YbbSportsRepository extends JpaRepository<YbbSports, String> {

    List<YbbSports> findByTaskId(String taskId);
    List<YbbSports> findByTaskIdOrderByDishId(String taskId);
    List<YbbSports> findByTaskIdOrderByLeagueIdAscDishIdAsc(String taskId);
}
