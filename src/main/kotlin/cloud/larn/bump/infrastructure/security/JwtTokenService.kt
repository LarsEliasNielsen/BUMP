package cloud.larn.bump.infrastructure.security

import cloud.larn.bump.application.port.IssuedToken
import cloud.larn.bump.application.port.TokenIssuer
import cloud.larn.bump.domain.model.Role
import cloud.larn.bump.domain.model.TenantId
import cloud.larn.bump.domain.model.UserId
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Component
class JwtTokenService(
    private val encoder: JwtEncoder,
    private val properties: JwtProperties,
) : TokenIssuer {

    override fun issue(
        userId: UserId,
        tenantId: TenantId,
        role: Role
    ): IssuedToken {
        val now = Instant.now()
        val expiresAt = now.plus(properties.expirationHours, ChronoUnit.HOURS)

        val header = JwsHeader.with(MacAlgorithm.HS256).build()
        val claims = JwtClaimsSet.builder()
            .subject(userId.value.toString())
            .claim("tenantId", tenantId.value.toString())
            .claim("role", role.name)
            .issuedAt(now)
            .expiresAt(expiresAt)
            .id(UUID.randomUUID().toString())
            .build()

        val jwt = encoder.encode(JwtEncoderParameters.from(header, claims))
        return IssuedToken(token = jwt.tokenValue, expiresAt = expiresAt)
    }
}
