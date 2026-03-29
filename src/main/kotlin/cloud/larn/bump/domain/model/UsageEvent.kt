package cloud.larn.bump.domain.model

import java.time.OffsetDateTime
import java.util.UUID

class UsageEvent(
    val id: UUID = UUID.randomUUID(),
    val customerId: CustomerId,
    val service: String,
    val product: String,
    val eventDateTime: OffsetDateTime,
    val idempotencyKey: IdempotencyKey,
) {
    // Aggregates use identity-based equality: two UsageEvents are the same if they share the same id,
    // regardless of their fields. This reflects the DDD rule that an aggregate's identity is stable
    // over its lifetime. Do not replace this with structural equality (data class).
    override fun equals(other: Any?): Boolean = other is UsageEvent && id == other.id
    override fun hashCode(): Int = id.hashCode()
}
