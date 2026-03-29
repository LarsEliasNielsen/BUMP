package cloud.larn.bump.application.usecase

import cloud.larn.bump.application.port.DomainEventPublisher
import cloud.larn.bump.domain.event.UsageRecorded
import cloud.larn.bump.domain.exception.DuplicateIdempotencyKeyException
import cloud.larn.bump.domain.model.UsageEvent
import cloud.larn.bump.domain.repository.UsageEventRepository
import org.springframework.stereotype.Service

@Service
class RecordUsageEvent(
    private val repository: UsageEventRepository,
    private val eventPublisher: DomainEventPublisher,
) {

    fun execute(command: RecordUsageEventCommand): RecordUsageEventResult {
        if (repository.existsByIdempotencyKey(command.idempotencyKey)) {
            return RecordUsageEventResult.Duplicate
        }

        val event = try {
            repository.save(
                UsageEvent(
                    customerId = command.customerId,
                    service = command.service,
                    product = command.product,
                    eventDateTime = command.eventDateTime,
                    idempotencyKey = command.idempotencyKey,
                )
            )
        } catch (_: DuplicateIdempotencyKeyException) {
            return RecordUsageEventResult.Duplicate
        }

        eventPublisher.publish(
            UsageRecorded(
                usageEventId = event.id,
                customerId = event.customerId,
                service = event.service,
                product = event.product,
                idempotencyKey = event.idempotencyKey,
                occurredAt = event.eventDateTime.toInstant(),
            )
        )

        return RecordUsageEventResult.Recorded(event)
    }
}
