package cloud.larn.bump.infrastructure.persistence

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.security.core.context.SecurityContextHolder
import java.util.Optional
import java.util.UUID

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
class JpaAuditingConfig {

    @Bean
    fun auditorProvider(): AuditorAware<UUID> = AuditorAware {
        // Returns empty for unauthenticated actions (e.g. self-service tenant registration).
        // Will be updated to extract the userId UUID from the JWT principal.
        val auth = SecurityContextHolder.getContext().authentication
        if (auth == null || !auth.isAuthenticated || auth.principal == "anonymousUser") {
            Optional.empty()
        } else {
            Optional.empty() // Placeholder until JWT principal is wired.
        }
    }
}
