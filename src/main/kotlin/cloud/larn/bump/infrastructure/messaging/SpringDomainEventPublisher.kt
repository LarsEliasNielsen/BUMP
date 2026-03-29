package cloud.larn.bump.infrastructure.messaging

import cloud.larn.bump.application.port.DomainEventPublisher
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class SpringDomainEventPublisher(
    private val publisher: ApplicationEventPublisher,
) : DomainEventPublisher {

    override fun publish(event: Any) = publisher.publishEvent(event)
}
