package com.ze.crawler.core.repository;

import com.ze.crawler.core.entity.FyEsports;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FyEsportsRepository extends JpaRepository<FyEsports, String> {

    List<FyEsports> findByTaskId(String taskId);
    List<FyEsports> findByTaskIdOrderByDishId(String taskId);
    List<FyEsports> findByTaskIdOrderByLeagueIdAscDishIdAsc(String taskId);
}
