package cloud.larn.bump.domain.model

import java.time.OffsetDateTime
import java.util.UUID

class UsageEvent(
    val id: UUID = UUID.randomUUID(),
    val tenantId: TenantId,
    val userId: UserId,
    val service: String,
    val product: String,
    val eventDateTime: OffsetDateTime,
    val idempotencyKey: IdempotencyKey,
) {
    override fun equals(other: Any?): Boolean = other is UsageEvent && id == other.id
    override fun hashCode(): Int = id.hashCode()
}
