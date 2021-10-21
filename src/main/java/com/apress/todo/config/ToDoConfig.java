package com.apress.todo.config;

import com.apress.todo.domain.ToDo;
import com.apress.todo.redis.ToDoConsumer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

@Configuration
public class ToDoConfig {
	
    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory
connectionFactory, MessageListenerAdapter toDoListenerAdapter, @Value("${todo.redis.topic}") String topic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(toDoListenerAdapter, new PatternTopic(topic));
        return container;
    }

    @Bean
    MessageListenerAdapter toDoListenerAdapter(ToDoConsumer consumer) {
    	MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(consumer);
    	Jackson2JsonRedisSerializer jsonSerializer = new Jackson2JsonRedisSerializer<>(ToDo.class);
    	ObjectMapper objectMapper = new ObjectMapper(); 
    	objectMapper.findAndRegisterModules();
    	objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);	
    	jsonSerializer.setObjectMapper(objectMapper);
    	messageListenerAdapter.setSerializer(jsonSerializer);
        return messageListenerAdapter;
    }

    @Bean
    RedisTemplate<String, ToDo> redisTemplate(RedisConnectionFactory connectionFactory){
    	RedisTemplate<String,ToDo> redisTemplate = new RedisTemplate<String,ToDo>();
    	redisTemplate.setConnectionFactory(connectionFactory);
    	Jackson2JsonRedisSerializer jsonSerializer = new Jackson2JsonRedisSerializer<>(ToDo.class);
    	ObjectMapper objectMapper = new ObjectMapper(); 
    	objectMapper.findAndRegisterModules();
    	objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);	
    	jsonSerializer.setObjectMapper(objectMapper);
        redisTemplate.setDefaultSerializer(jsonSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}