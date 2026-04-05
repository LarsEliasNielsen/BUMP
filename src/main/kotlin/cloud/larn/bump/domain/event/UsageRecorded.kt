package cloud.larn.bump.domain.event

import cloud.larn.bump.domain.model.IdempotencyKey
import cloud.larn.bump.domain.model.TenantId
import cloud.larn.bump.domain.model.UserId
import java.time.Instant
import java.util.UUID

data class UsageRecorded(
    val eventId: UUID = UUID.randomUUID(),
    val usageEventId: UUID,
    val tenantId: TenantId,
    val userId: UserId,
    val service: String,
    val product: String,
    val idempotencyKey: IdempotencyKey,
    val occurredAt: Instant,
) : DomainEvent
