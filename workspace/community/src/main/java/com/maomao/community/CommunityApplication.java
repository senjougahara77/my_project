package com.maomao.community;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
@MapperScan({"com.maomao.community.dao"})
public class CommunityApplication {

	@PostConstruct
	public void init() {
		// 解决netty启动冲突的问题 redis和es的冲突
		// 基于netty4utils.setAvailableProcessors方法
		System.setProperty("es.set.netty.runtime.available.processors", "false");
	}

	public static void main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
	}

}
