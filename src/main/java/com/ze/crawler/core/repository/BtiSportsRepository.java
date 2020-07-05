package com.ze.crawler.core.repository;

import com.ze.crawler.core.entity.BtiSports;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BtiSportsRepository extends JpaRepository<BtiSports, String> {

    List<BtiSports> findByTaskId(String taskId);
    List<BtiSports> findByTaskIdOrderByDishId(String taskId);
    List<BtiSports> findByTaskIdOrderByLeagueIdAscDishIdAsc(String taskId);
}
