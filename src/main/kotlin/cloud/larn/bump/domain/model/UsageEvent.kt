package cloud.larn.bump.domain.model

import java.time.OffsetDateTime
import java.util.UUID

data class UsageEvent(
    val id: UUID = UUID.randomUUID(),
    val userId: String,
    val service: String,
    val product: String,
    val eventDateTime: OffsetDateTime,
)
