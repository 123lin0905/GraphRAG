package com.sanxingdui.twin.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Knife4j 接口文档配置，访问 /doc.html
 */
@Configuration
public class OpenApiConfig {

    private static final String SCHEME = "BearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("三星堆文物数字孪生与智能导览系统 API")
                        .description("基于3D高斯泼溅与GraphRAG的三星堆文物数字孪生与智能导览系统后端接口文档。"
                                + "登录后在右上角“Authorize”中填入 JWT 令牌即可调试需要认证的接口。")
                        .version("1.0.0")
                        .contact(new Contact().name("sanxingdui-twin")))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME))
                .components(new Components().addSecuritySchemes(SCHEME,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}
