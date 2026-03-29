package cloud.larn.bump.domain.event

import cloud.larn.bump.domain.model.CustomerId
import cloud.larn.bump.domain.model.IdempotencyKey
import java.time.Instant
import java.util.UUID

data class UsageRecorded(
    val eventId: UUID = UUID.randomUUID(),
    val usageEventId: UUID,
    val customerId: CustomerId,
    val service: String,
    val product: String,
    val idempotencyKey: IdempotencyKey,
    val occurredAt: Instant,
) : DomainEvent
