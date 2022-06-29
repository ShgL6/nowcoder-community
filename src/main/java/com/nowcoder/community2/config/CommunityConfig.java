package com.nowcoder.community2.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.Collections;
import java.util.Properties;

@Configuration
public class CommunityConfig {

    /**
     * 验证码
     * @return
     */
    @Bean
    public Producer kaptchaProducer(){
        Properties properties = new Properties();
        properties.setProperty("kaptcha.image.width","100" );
        properties.setProperty("kaptcha.image.height" ,"50");
        properties.setProperty("kaptcha.textproducer.font.size", "32");
        properties.setProperty("kaptcha.textproducer.font.color","0,0,0");
        properties. setProperty("kaptcha.textproducer.char.string", "123456789QWERTYUIOPASDFGHJKLZXCVBNM");
        properties.setProperty("kaptcha.textproducer.char.length","4");
        properties.setProperty("kaptcha.noise.impl" , "com.google.code.kaptcha.impl.NoNoise");

        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Config config = new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }



    /**
     * redisTemplate
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){

        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // key 的序列化方式
        redisTemplate.setKeySerializer(RedisSerializer.string());
        // value 的序列化方式
        redisTemplate.setValueSerializer(RedisSerializer.json());


        // hash 的 key 的序列化方式
        redisTemplate.setHashKeySerializer(RedisSerializer.json());
        // hash 的 value 的序列化方式
        redisTemplate.setHashValueSerializer(RedisSerializer.json());

        redisTemplate.afterPropertiesSet();

        return  redisTemplate;
    }






}
