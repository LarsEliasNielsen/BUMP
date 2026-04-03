package cloud.larn.bump.api

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
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
}
