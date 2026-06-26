package com.printclub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI 配置
 *
 * <p>访问：http://localhost:8080/doc.html</p>
 *
 * <p>v3：去掉了 SecurityScheme 配置（多此一举，反而把 Knife4j 搞复杂）</p>
 * <p>改用：直接拿登录接口的 token，调其他接口时手动在请求头加 Authorization</p>
 *
 * @author D
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI printClubOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("3D 打印科创会管理系统 API")
                        .description("社团内部管理系统后端接口文档")
                        .version("1.0.0")
                        .contact(new Contact().name("D").email("dev@printclub.com"))
                        .license(new License().name("MIT")));
    }
}