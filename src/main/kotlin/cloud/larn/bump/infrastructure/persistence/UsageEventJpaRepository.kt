package cloud.larn.bump.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UsageEventJpaRepository : JpaRepository<UsageEventEntity, UUID> {
    fun existsByIdempotencyKey(idempotencyKey: String): Boolean
}
