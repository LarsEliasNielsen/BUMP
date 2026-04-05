package cloud.larn.bump.infrastructure.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "bump.security.jwt")
data class JwtProperties(
    // Must be a base64url-encoded, cryptographically random value of at least 32 bytes (256 bits).
    // Generate with: openssl rand -base64 32 | tr '+/' '-_' | tr -d '='
    val secret: String,
    val expirationHours: Long = 24,
)
