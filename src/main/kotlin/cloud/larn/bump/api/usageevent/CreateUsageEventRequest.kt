package cloud.larn.bump.api.usageevent

import cloud.larn.bump.api.OffsetDateTimeSerializer
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class CreateUsageEventRequest(
    @field:NotBlank val customerId: String,
    @field:NotBlank val service: String,
    @field:NotBlank val product: String,
    @field:NotNull @Serializable(with = OffsetDateTimeSerializer::class) val eventDateTime: OffsetDateTime,
    @field:NotBlank val idempotencyKey: String,
)
