package cloud.larn.bump.application.usecase

import cloud.larn.bump.domain.model.CustomerId
import cloud.larn.bump.domain.model.IdempotencyKey
import java.time.OffsetDateTime

data class RecordUsageEventCommand(
    val customerId: CustomerId,
    val service: String,
    val product: String,
    val eventDateTime: OffsetDateTime,
    val idempotencyKey: IdempotencyKey,
)
