package cloud.larn.bump.api.usageevent

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime

data class CreateUsageEventRequest(
    @field:NotBlank val userId: String,
    @field:NotBlank val service: String,
    @field:NotBlank val product: String,
    @field:NotNull val eventDateTime: OffsetDateTime,
)
