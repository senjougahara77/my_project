package com.maomao.community;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({"com.maomao.community.dao"})
public class CommunityApplication {

	public static void main(String[] args) {

		SpringApplication.run(CommunityApplication.class, args);
	}

}
