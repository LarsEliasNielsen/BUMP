package cloud.larn.bump.infrastructure.persistence

import cloud.larn.bump.domain.exception.DuplicateIdempotencyKeyException
import cloud.larn.bump.domain.model.IdempotencyKey
import cloud.larn.bump.domain.model.TenantId
import cloud.larn.bump.domain.model.UsageEvent
import cloud.larn.bump.domain.model.UserId
import cloud.larn.bump.domain.repository.UsageEventRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UsageEventRepositoryAdapter(
    private val jpa: UsageEventJpaRepository,
) : UsageEventRepository {

    override fun save(event: UsageEvent): UsageEvent =
        try {
            jpa.save(event.toEntity()).toDomain()
        } catch (_: DataIntegrityViolationException) {
            throw DuplicateIdempotencyKeyException(event.idempotencyKey)
        }

    override fun findById(id: UUID): UsageEvent? =
        jpa.findById(id).orElse(null)?.toDomain()

    override fun existsByIdempotencyKey(key: IdempotencyKey): Boolean =
        jpa.existsByIdempotencyKey(key.value)

    private fun UsageEvent.toEntity() = UsageEventEntity(
        id = id,
        tenantId = tenantId.value,
        userId = userId.value,
        service = service,
        product = product,
        eventDateTime = eventDateTime,
        idempotencyKey = idempotencyKey.value,
    )

    private fun UsageEventEntity.toDomain() = UsageEvent(
        id = id,
        tenantId = TenantId(tenantId),
        userId = UserId(userId),
        service = service,
        product = product,
        eventDateTime = eventDateTime,
        idempotencyKey = IdempotencyKey(idempotencyKey),
    )
}
