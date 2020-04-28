package com.ze.crawler.core.repository;

import com.ze.crawler.core.entity.RgEsports;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RgEsportsRepository extends JpaRepository<RgEsports, String> {

    List<RgEsports> findByTaskId(String taskId);
    List<RgEsports> findByTaskIdOrderByDishId(String taskId);
    List<RgEsports> findByTaskIdOrderByLeagueIdAscDishIdAsc(String taskId);
}
