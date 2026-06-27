package com.printclub.config;

import com.printclub.common.interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC 配置
 *
 * <p>职责：</p>
 * <ol>
 *   <li>注册 JwtInterceptor 到 {@code /api/**}</li>
 *   <li>把本地 {@code ./uploads/} 映射成可访问的 {@code /uploads/**}</li>
 *   <li>找不到图片时 fallback 到项目内置的占位图（避免 500）</li>
 * </ol>
 *
 * @author D
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                // 公开路径（不需要 token）
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/logout",
                        "/error",
                        "/doc.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 本地文件访问：访问 /uploads/xxx.jpg 时，去 ./uploads/xxx.jpg 取
        // 找不到时 Spring 会自动返回 404，但 Vite proxy 把 404 当 500
        // 修法：把缺图请求重定向到 classpath:static/placeholder.png（必须存在）
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(
                        "file:" + uploadDir,
                        "classpath:/static/"
                );
    }
}