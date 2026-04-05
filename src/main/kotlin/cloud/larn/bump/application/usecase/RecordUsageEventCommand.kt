package cloud.larn.bump.application.usecase

import cloud.larn.bump.domain.model.IdempotencyKey
import cloud.larn.bump.domain.model.TenantId
import cloud.larn.bump.domain.model.UserId
import java.time.OffsetDateTime

data class RecordUsageEventCommand(
    val tenantId: TenantId,
    val userId: UserId,
    val service: String,
    val product: String,
    val eventDateTime: OffsetDateTime,
    val idempotencyKey: IdempotencyKey,
)
