package com.printclub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS 跨域配置（2026-06-29 修复版）
 *
 * <p>Spring 6 的 WebMvcConfigurer.addCorsMappings().allowedOriginPatterns("https://*.pages.dev")
 * 实际上不工作——token-level 通配符不会匹配 https://&lt;sub&gt;.pages.dev。
 * 改用 Spring 6 推荐的 @Bean CorsConfigurationSource 风格，setAllowedOriginPatterns("*")
 * 才能正确接受所有 origin。</p>
 *
 * <p>前端用 JWT（Authorization 头），不传 cookie，所以 setAllowCredentials(false) 即可。</p>
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // ★ 关键：setAllowedOriginPatterns("*") 接受所有 origin（包括 *.pages.dev 等 subdomain）
        // 用 setAllowedOriginPatterns 而不是 setAllowedOrigins，因为后者不允许通配符
        config.setAllowedOriginPatterns(List.of("*"));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));

        // 前端 axios 用 JWT（Authorization 头），不依赖 cookie 跨域传递
        // 所以这里可以 false；如果以后需要 cookie，再改成 true 并配合具体 origin
        config.setAllowCredentials(false);

        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}