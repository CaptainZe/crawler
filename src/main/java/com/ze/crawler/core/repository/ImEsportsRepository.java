package com.ze.crawler.core.repository;

import com.ze.crawler.core.entity.ImEsports;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImEsportsRepository extends JpaRepository<ImEsports, String> {

    List<ImEsports> findByTaskId(String taskId);
    List<ImEsports> findByTaskIdOrderByDishId(String taskId);
    List<ImEsports> findByTaskIdOrderByLeagueIdAscDishIdAsc(String taskId);
}
