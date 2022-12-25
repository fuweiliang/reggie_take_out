package com.itheima.springredisdemo.config;

import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig extends CachingConfigurerSupport {

}
