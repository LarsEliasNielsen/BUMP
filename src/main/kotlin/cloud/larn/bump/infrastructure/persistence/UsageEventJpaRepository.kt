package cloud.larn.bump.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

internal interface UsageEventJpaRepository : JpaRepository<UsageEventEntity, UUID>
