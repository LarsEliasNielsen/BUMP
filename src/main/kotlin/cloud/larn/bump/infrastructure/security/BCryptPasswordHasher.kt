package cloud.larn.bump.infrastructure.security

import cloud.larn.bump.application.port.PasswordHasher
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class BCryptPasswordHasher(private val encoder: PasswordEncoder) : PasswordHasher {
    override fun hash(plainText: String): String = encoder.encode(plainText)
        ?: error("BCryptPasswordEncoder returned null for non-null input")
}
