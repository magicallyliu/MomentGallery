package com.liuh.gallerybackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author LiuH
 * @Date 2025/4/24 下午11:07
 * @PackageName com.liuh.gallerybackend.config
 * @ClassName CorsConfig
 * @Version
 * @Description 配置全局跨域
 */

@SuppressWarnings("all")
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 覆盖所有请求
        //匹配所有路径
        registry.addMapping("/**")
                // 允许发送 Cookie
                .allowCredentials(true)
                // 放行哪些域名（必须用 patterns，否则 * 会和 allowCredentials 冲突）
                .allowedOriginPatterns("*")
                //允许的HTTP方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                //允许所有请求头
                .allowedHeaders("*")
                //暴露所有响应头给客户端
                .exposedHeaders("*");
    }
}

