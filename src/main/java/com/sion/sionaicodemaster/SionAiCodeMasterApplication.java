package com.sion.sionaicodemaster;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication
@MapperScan("com.sion.sionaicodemaster.mapper")
public class SionAiCodeMasterApplication {

    public static void main(String[] args) {
        SpringApplication.run(SionAiCodeMasterApplication.class, args);
    }

}
