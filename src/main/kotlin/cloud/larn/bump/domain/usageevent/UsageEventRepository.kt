package cloud.larn.bump.domain.usageevent

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UsageEventRepository: JpaRepository<UsageEvent, UUID>
