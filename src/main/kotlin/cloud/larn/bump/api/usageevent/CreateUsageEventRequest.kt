package cloud.larn.bump.api.usageevent

import cloud.larn.bump.api.OffsetDateTimeSerializer
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
@Schema(description = "Request body for recording a usage event")
data class CreateUsageEventRequest(
    @field:NotBlank
    @Schema(
        description = "Identifier of the customer being billed for this usage",
        example = "customer-123")
    val customerId: String,

    @field:NotBlank
    @Schema(
        description = "The service being billed for this usage (e.g. storage, compute)",
        example = "compute")
    val service: String,

    @field:NotBlank
    @Schema(
        description = "The specific product or SKU within the service",
        example = "gpu-instance")
    val product: String,

    @field:NotNull
    @Serializable(with = OffsetDateTimeSerializer::class)
    @Schema(
        description = "When the usage occurred (ISO 8601 with offset)",
        example = "2026-01-15T14:30:00+01:00")
    val eventDateTime: OffsetDateTime,

    @field:NotBlank
    @Schema(
        description = "Client-generated unique key to prevent duplicate event recording. " +
            "Re-submitting with the same key returns 409 instead of recording a duplicate.",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    val idempotencyKey: String,
)
