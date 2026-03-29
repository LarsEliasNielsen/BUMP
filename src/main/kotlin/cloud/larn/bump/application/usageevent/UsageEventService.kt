package cloud.larn.bump.application.usageevent

import cloud.larn.bump.domain.model.UsageEvent
import cloud.larn.bump.domain.repository.UsageEventRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class UsageEventService(private val repository: UsageEventRepository) {

    fun create(userId: String, service: String, product: String, eventDateTime: OffsetDateTime): UsageEvent =
        repository.save(UsageEvent(userId = userId, service = service, product = product, eventDateTime = eventDateTime))
}
