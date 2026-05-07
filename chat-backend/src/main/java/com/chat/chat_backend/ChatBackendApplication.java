package com.chat.chat_backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.chat.chat_backend.mapper")
public class ChatBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(ChatBackendApplication.class, args);
	}
}