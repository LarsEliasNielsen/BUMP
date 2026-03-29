package cloud.larn.bump.application.port

import cloud.larn.bump.domain.event.DomainEvent

interface DomainEventPublisher {
    fun publish(event: DomainEvent)
}
