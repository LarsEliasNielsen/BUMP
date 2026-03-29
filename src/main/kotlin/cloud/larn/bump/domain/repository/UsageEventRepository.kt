package cloud.larn.bump.domain.repository

import cloud.larn.bump.domain.model.UsageEvent
import java.util.UUID

interface UsageEventRepository {
    fun save(event: UsageEvent): UsageEvent
    fun findById(id: UUID): UsageEvent?
}
