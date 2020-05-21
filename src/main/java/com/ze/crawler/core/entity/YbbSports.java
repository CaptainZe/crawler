package com.ze.crawler.core.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "t_sports_ybb", schema = "crawler")
public class YbbSports extends Sports {
}
