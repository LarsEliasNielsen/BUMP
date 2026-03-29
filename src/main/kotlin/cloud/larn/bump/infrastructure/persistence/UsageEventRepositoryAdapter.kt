package cloud.larn.bump.infrastructure.persistence

import cloud.larn.bump.domain.model.CustomerId
import cloud.larn.bump.domain.model.IdempotencyKey
import cloud.larn.bump.domain.model.UsageEvent
import cloud.larn.bump.domain.repository.UsageEventRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UsageEventRepositoryAdapter(
    private val jpa: UsageEventJpaRepository,
) : UsageEventRepository {

    override fun save(event: UsageEvent): UsageEvent =
        jpa.save(event.toEntity()).toDomain()

    override fun findById(id: UUID): UsageEvent? =
        jpa.findById(id).orElse(null)?.toDomain()

    override fun existsByIdempotencyKey(key: IdempotencyKey): Boolean =
        jpa.existsByIdempotencyKey(key.value)

    private fun UsageEvent.toEntity() = UsageEventEntity(
        id = id,
        userId = customerId.value,
        service = service,
        product = product,
        eventDateTime = eventDateTime,
        idempotencyKey = idempotencyKey.value,
    )

    private fun UsageEventEntity.toDomain() = UsageEvent(
        id = id,
        customerId = CustomerId(userId),
        service = service,
        product = product,
        eventDateTime = eventDateTime,
        idempotencyKey = IdempotencyKey(idempotencyKey),
    )
}
