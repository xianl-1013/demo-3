package com.zhiyun.meeting.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 接口文档配置
 *
 * 作用：
 * 1. 配置 Swagger 页面标题
 * 2. 配置接口文档版本
 * 3. 配置 Authorization Token 认证
 *
 * 配置完成后，Swagger 页面右上角会出现 Authorize 按钮。
 */
@Configuration
public class OpenApiConfig {

    /**
     * Swagger 认证名称
     *
     * 注意：
     * 这里的名字要和 SecurityRequirement 里的名字一致。
     */
    private static final String SECURITY_SCHEME_NAME = "Authorization";

    /**
     * 创建 OpenAPI 文档配置
     *
     * @return OpenAPI 配置对象
     */
    @Bean
    public OpenAPI zhiyunMeetingOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("智云会议后端接口文档")
                        .version("1.0.0")
                        .description("纯血鸿蒙会议项目后端接口文档，包含登录、会议、成员、聊天、会控、WebSocket 信令等接口"))

                // 给所有接口添加全局 Authorization 认证要求
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))

                // 定义 Authorization 请求头格式
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                );
    }
}