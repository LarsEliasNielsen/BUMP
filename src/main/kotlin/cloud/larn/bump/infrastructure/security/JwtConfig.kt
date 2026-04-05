package cloud.larn.bump.infrastructure.security

import com.nimbusds.jose.jwk.source.ImmutableSecret
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import java.util.Base64
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class JwtConfig {

    @Bean
    fun jwtEncoder(properties: JwtProperties): JwtEncoder =
        NimbusJwtEncoder(ImmutableSecret(secretKey(properties)))

    @Bean
    fun jwtDecoder(properties: JwtProperties): JwtDecoder =
        NimbusJwtDecoder.withSecretKey(secretKey(properties))
            .macAlgorithm(MacAlgorithm.HS256)
            .build()

    private fun secretKey(properties: JwtProperties): SecretKeySpec {
        val keyBytes = Base64.getUrlDecoder().decode(properties.secret)
        require(keyBytes.size >= 32) {
            "JWT secret must be at least 256 bits (32 bytes)"
        }
        return SecretKeySpec(keyBytes, "HmacSHA256")
    }
}
