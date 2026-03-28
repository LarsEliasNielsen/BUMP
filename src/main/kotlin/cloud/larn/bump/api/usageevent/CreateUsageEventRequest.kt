package cloud.larn.bump.api.usageevent

import java.time.OffsetDateTime

data class CreateUsageEventRequest(
    val userId: String,
    val service: String,
    val product: String,
    val eventDateTime: OffsetDateTime,
)
