package com.ze.crawler.core.repository;

import com.ze.crawler.core.entity.PbEsports;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PbEsportsRepository extends JpaRepository<PbEsports, String> {

    List<PbEsports> findByTaskId(String taskId);
    List<PbEsports> findByTaskIdOrderByDishId(String taskId);
    List<PbEsports> findByTaskIdOrderByLeagueIdAscDishIdAsc(String taskId);
}
