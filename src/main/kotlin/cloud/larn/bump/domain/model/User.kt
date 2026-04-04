package cloud.larn.bump.domain.model

import java.time.Instant
import java.util.UUID

class User(
    val id: UserId = UserId(UUID.randomUUID()),
    val tenantId: TenantId,
    val email: Email,
    val passwordHash: String,
    val role: Role,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val deletedAt: Instant? = null,
) {
    override fun equals(other: Any?): Boolean = other is User && id == other.id
    override fun hashCode(): Int = id.hashCode()
}
