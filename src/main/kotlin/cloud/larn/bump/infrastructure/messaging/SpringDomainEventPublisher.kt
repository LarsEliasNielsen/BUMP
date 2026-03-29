package cloud.larn.bump.infrastructure.messaging

import cloud.larn.bump.application.port.DomainEventPublisher
import cloud.larn.bump.domain.event.DomainEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class SpringDomainEventPublisher(
    private val publisher: ApplicationEventPublisher,
) : DomainEventPublisher {

    override fun publish(event: DomainEvent) = publisher.publishEvent(event)
}
