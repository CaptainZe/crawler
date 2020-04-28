package com.ze.crawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@ComponentScan(basePackages = "com")
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.ze.crawler.core.repository")
@EntityScan(basePackages = "com.ze.crawler.core.entity")
@EnableTransactionManagement
@EnableScheduling
public class CrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrawlerApplication.class, args);
    }
}
