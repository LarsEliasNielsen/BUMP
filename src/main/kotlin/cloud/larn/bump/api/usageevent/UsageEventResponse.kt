package cloud.larn.bump.api.usageevent

import cloud.larn.bump.api.OffsetDateTimeSerializer
import cloud.larn.bump.api.UUIDSerializer
import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import java.util.UUID

@Serializable
@Schema(description = "A recorded usage event")
data class UsageEventResponse(
    @Serializable(with = UUIDSerializer::class)
    @Schema(
        description = "System-assigned unique identifier for this usage event",
        example = "e29b6e3a-1234-4c8f-9abc-2f3d5e6a7b8c")
    val id: UUID,

    @Schema(
        description = "Identifier of the customer being billed for this usage",
        example = "customer-123")
    val customerId: String,

    @Schema(
        description = "The service being billed for this usage",
        example = "compute")
    val service: String,

    @Schema(
        description = "The specific product or SKU within the service",
        example = "gpu-instance")
    val product: String,

    @Serializable(with = OffsetDateTimeSerializer::class)
    @Schema(
        description = "When the usage occurred (ISO 8601 with offset)",
        example = "2026-01-15T14:30:00+01:00")
    val eventDateTime: OffsetDateTime,

    @Schema(
        description = "The idempotency key supplied by the client at ingestion time",
        example = "550e8400-e29b-41d4-a716-446655440000")
    val idempotencyKey: String,
)
