package com.ze.crawler.core.repository;

import com.ze.crawler.core.entity.TfEsports;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TfEsportsRepository extends JpaRepository<TfEsports, String> {

    List<TfEsports> findByTaskId(String taskId);
    List<TfEsports> findByTaskIdOrderByDishId(String taskId);
    List<TfEsports> findByTaskIdOrderByLeagueIdAscDishIdAsc(String taskId);
}
