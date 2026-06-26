package com.printclub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类
 *
 * @author D
 */
@SpringBootApplication
@MapperScan("com.printclub.module.**.mapper")
public class PrintClubApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrintClubApplication.class, args);
        System.out.println("""

                ====================================================
                   3D 打印科创会管理系统 - 后端启动成功
                   接口文档：http://localhost:8080/doc.html
                ====================================================
                """);
    }
}