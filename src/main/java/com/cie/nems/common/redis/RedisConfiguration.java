package com.cie.nems.common.redis;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisConfiguration {

	@Bean(name = "defaultRedisTemplate")
	public <T> RedisTemplate<String, T> redisTemplate(RedisConnectionFactory factory) {
		RedisTemplate<String, T> template = new RedisTemplate<String, T>();
		template.setConnectionFactory(factory);
		template.setDefaultSerializer(new StringRedisSerializer());
		template.setKeySerializer(new StringRedisSerializer());
		template.setHashKeySerializer(new StringRedisSerializer());
		return template;
	}

}
