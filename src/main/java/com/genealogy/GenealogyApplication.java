package com.genealogy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication
@EnableCaching
public class GenealogyApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext application = SpringApplication.run(GenealogyApplication.class, args);
        Environment environment = application.getEnvironment();
        String port = environment.getProperty("server.port", "8080");

        System.out.println("========================================");
        System.out.println("家谱管理系统启动成功！");
        System.out.println("访问地址: http://localhost:" + port);
        System.out.println("========================================");
    }
}
