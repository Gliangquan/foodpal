package com.jcen.medpal;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 主类（项目启动入口）
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@SpringBootApplication
@MapperScan("com.jcen.medpal.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@Slf4j
public class FoodPalMainApplication {

    @Value("${app.public-base-url:http://107.148.176.142:9040}")
    private String publicBaseUrl;

    public static void main(String[] args) {
        SpringApplication.run(FoodPalMainApplication.class, args);
    }

    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void logSwaggerAddress() {
        log.info("接口文档访问：{}/api/doc.html#/home", publicBaseUrl);
    }

}
