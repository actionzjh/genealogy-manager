package com.genealogy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GenealogyApplication {
    public static void main(String[] args) {
        SpringApplication.run(GenealogyApplication.class, args);
        System.out.println("========================================");
        System.out.println("家谱管理系统启动成功！");
        System.out.println("访问地址: http://localhost:8080");
        System.out.println("========================================");
    }
}
