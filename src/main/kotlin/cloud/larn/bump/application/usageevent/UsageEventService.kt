package cloud.larn.bump.application.usageevent

import cloud.larn.bump.domain.usageevent.UsageEvent
import cloud.larn.bump.domain.usageevent.UsageEventRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class UsageEventService(private val repository: UsageEventRepository) {

    fun create(userId: String, service: String, product: String, eventDateTime: OffsetDateTime): UsageEvent =
        repository.save(UsageEvent(userId = userId, service = service, product = product, eventDateTime = eventDateTime))
}
