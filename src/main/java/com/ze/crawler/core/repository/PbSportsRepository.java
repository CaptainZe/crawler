package com.ze.crawler.core.repository;

import com.ze.crawler.core.entity.PbSports;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PbSportsRepository extends JpaRepository<PbSports, String> {

    List<PbSports> findByTaskId(String taskId);
    List<PbSports> findByTaskIdOrderByDishId(String taskId);
    List<PbSports> findByTaskIdOrderByLeagueIdAscDishIdAsc(String taskId);
}
