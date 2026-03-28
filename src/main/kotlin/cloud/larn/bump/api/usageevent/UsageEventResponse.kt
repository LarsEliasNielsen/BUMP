package cloud.larn.bump.api.usageevent

import java.time.OffsetDateTime
import java.util.UUID

data class UsageEventResponse(
    val id: UUID,
    val userId: String,
    val service: String,
    val product: String,
    val eventDateTime: OffsetDateTime,
)
