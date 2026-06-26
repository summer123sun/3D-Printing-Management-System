package com.printclub.config;

import com.printclub.common.interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 *
 * <p>职责：</p>
 * <ol>
 *   <li>注册 JwtInterceptor 到 {@code /api/**}</li>
 *   <li>把本地 {@code ./uploads/} 映射成可访问的 {@code /uploads/**}</li>
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
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir);
    }
}