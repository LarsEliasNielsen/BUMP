package cloud.larn.bump.application.port

interface DomainEventPublisher {
    fun publish(event: Any)
}
