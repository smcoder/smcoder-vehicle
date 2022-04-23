package org.smcoder.vehicle.config;

import org.smcoder.vehicle.redis.RedisKeyTimeRegion;
import org.smcoder.vehicle.redis.RedisValueTrajectorPathList;
import org.smcoder.vehicle.redis.RedisObjectSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfig {
    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        return new JedisConnectionFactory();
    }

    @Bean
    public RedisTemplate<RedisKeyTimeRegion, RedisValueTrajectorPathList> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<RedisKeyTimeRegion, RedisValueTrajectorPathList> template = new RedisTemplate();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new RedisObjectSerializer());
        template.setValueSerializer(new RedisObjectSerializer());
        return template;
    }
}
