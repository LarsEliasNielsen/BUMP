package cloud.larn.bump.api.usageevent

import cloud.larn.bump.api.OffsetDateTimeSerializer
import cloud.larn.bump.api.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import java.util.UUID

@Serializable
data class UsageEventResponse(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    val userId: String,
    val service: String,
    val product: String,
    @Serializable(with = OffsetDateTimeSerializer::class) val eventDateTime: OffsetDateTime,
)
