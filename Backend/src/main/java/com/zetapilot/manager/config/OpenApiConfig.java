package com.zetapilot.manager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI managerOpenApi() {
        return new OpenAPI().info(new Info()
                .title("ZetaPilotAI Manager API")
                .version("v1")
                .description("Management API for the ZetaPilotAI Visual Studio Code extension."));
    }
}