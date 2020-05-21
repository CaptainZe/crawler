package com.ze.crawler.core.repository;

import com.ze.crawler.core.entity.ImSports;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImSportsRepository extends JpaRepository<ImSports, String> {

    List<ImSports> findByTaskId(String taskId);
    List<ImSports> findByTaskIdOrderByDishId(String taskId);
    List<ImSports> findByTaskIdOrderByLeagueIdAscDishIdAsc(String taskId);
}
