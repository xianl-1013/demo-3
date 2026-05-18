package com.zhiyun.meeting.common.config;

import com.zhiyun.meeting.common.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置
 *
 * 当前作用：
 * 1. 注册登录 token 拦截器
 * 2. 配置全局跨域 CORS
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebMvcConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    /**
     * 注册登录拦截器
     *
     * addPathPatterns("/**") 表示默认拦截所有 HTTP 请求。
     * excludePathPatterns 表示放行不需要登录的接口。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/system/config",
                        // 登录接口必须放行，否则没法登录
                        "/auth/login",

                        // 测试接口放行
                        "/test/**",

                        // Swagger / OpenAPI 页面放行
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs",
                        "/v3/api-docs/**",

                        // 静态资源放行
                        "/favicon.ico",
                        "/error"
                );
    }

    /**
     * 全局跨域配置
     *
     * 解决问题：
     * 1. 浏览器 H5 调用后端接口跨域
     * 2. 鸿蒙 WebView / H5 调接口跨域
     * 3. Authorization 请求头被浏览器拦截
     *
     * 注意：
     * 第一版开发阶段允许所有来源。
     * 正式上线时建议改成指定域名。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 开发阶段允许所有来源
                .allowedOriginPatterns("*")

                // 允许的请求方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")

                // 允许所有请求头，包括 Authorization
                .allowedHeaders("*")

                // 前端可以读取的响应头
                .exposedHeaders("Authorization", "Content-Type")

                // 不使用 Cookie，token 走 Authorization header
                .allowCredentials(false)

                // 预检请求缓存时间，单位秒
                .maxAge(3600);
    }
}