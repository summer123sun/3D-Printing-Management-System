package com.printclub.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS 跨域配置
 *
 * <p>开发环境允许 Vue dev server (http://localhost:5173) 跨域访问</p>
 *
 * @author D
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                        "http://localhost:5173",  // Vite dev
                        "http://localhost:8080",  // 同源部署
                        "http://127.0.0.1:*",     // 本机任意端口
                        // ====== 生产环境部署域名（2026-06-28 部署到阿里云 ECS + Cloudflare Pages） ======
                        "http://8.137.80.194",                        // 阿里云 ECS 公网 IP（直接访问）
                        "https://*.pages.dev",                        // Cloudflare Pages 自动域名
                        "https://3d-print.club",                      // 自定义域名（待定）
                        "https://www.3d-print.club"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }
}