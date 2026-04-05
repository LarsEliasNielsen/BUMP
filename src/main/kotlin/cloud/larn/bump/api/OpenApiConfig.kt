package cloud.larn.bump.api

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openApi(): OpenAPI = OpenAPI()
        .info(Info()
            .title("BUMP — Billing and Usage Metering Platform")
            .description("API for recording and querying usage events in a usage-based billing platform. " +
                    "All write operations are idempotent via the `idempotencyKey` field.")
            .version("0.0.1-SNAPSHOT"))
        .components(Components()
            .addSecuritySchemes("bearerAuth", SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT obtained from POST /auth/login. Include as: Authorization: Bearer <token>")))
}
