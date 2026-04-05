package cloud.larn.bump.application.port

import cloud.larn.bump.domain.model.Role
import cloud.larn.bump.domain.model.TenantId
import cloud.larn.bump.domain.model.UserId
import java.time.Instant

interface TokenIssuer {
    fun issue(userId: UserId, tenantId: TenantId, role: Role): IssuedToken
}

data class IssuedToken(val token: String, val expiresAt: Instant)
