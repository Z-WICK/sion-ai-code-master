package com.sion.sionaicodemaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication
public class SionAiCodeMasterApplication {

    public static void main(String[] args) {
        SpringApplication.run(SionAiCodeMasterApplication.class, args);
    }

}
