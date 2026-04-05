package cloud.larn.bump.infrastructure.security

import com.nimbusds.jose.jwk.source.ImmutableSecret
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import java.util.Base64
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class JwtConfig {

    @Bean
    fun jwtEncoder(properties: JwtProperties): JwtEncoder {
        // Secret is base64url-encoded; getUrlDecoder handles both padded and unpadded input.
        val keyBytes = Base64.getUrlDecoder().decode(properties.secret)
        require(keyBytes.size >= 32) { "JWT secret must be at least 256 bits (32 bytes) — generate one with: openssl rand -base64 32 | tr '+/' '-_' | tr -d '='" }
        val key = SecretKeySpec(keyBytes, "HmacSHA256")
        return NimbusJwtEncoder(ImmutableSecret(key))
    }
}
