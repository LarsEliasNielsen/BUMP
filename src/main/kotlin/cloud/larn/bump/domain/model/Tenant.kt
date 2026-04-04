package cloud.larn.bump.domain.model

import java.time.Instant
import java.util.UUID

class Tenant(
    val id: TenantId = TenantId(UUID.randomUUID()),
    val name: String,
    val status: TenantStatus = TenantStatus.ACTIVE,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val deletedAt: Instant? = null,
) {
    override fun equals(other: Any?): Boolean = other is Tenant && id == other.id
    override fun hashCode(): Int = id.hashCode()
}
