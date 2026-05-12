package com.chat.chat_backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({"com.chat.chat_backend.modules.user.mapper",
             "com.chat.chat_backend.modules.friend.mapper",
             "com.chat.chat_backend.modules.group.mapper",
             "com.chat.chat_backend.modules.message.mapper",
             "com.chat.chat_backend.modules.emoji.mapper",
             "com.chat.chat_backend.modules.impression.mapper",
             "com.chat.chat_backend.modules.notification.mapper"})
public class ChatBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(ChatBackendApplication.class, args);
	}
}