package cloud.larn.bump.application.usecase

import cloud.larn.bump.application.port.DomainEventPublisher
import cloud.larn.bump.domain.event.UsageRecorded
import cloud.larn.bump.domain.exception.DuplicateIdempotencyKeyException
import cloud.larn.bump.domain.model.IdempotencyKey
import cloud.larn.bump.domain.model.TenantId
import cloud.larn.bump.domain.model.UsageEvent
import cloud.larn.bump.domain.model.UserId
import cloud.larn.bump.domain.repository.UsageEventRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RecordUsageEventTest {

    private val repository: UsageEventRepository = mock()
    private val eventPublisher: DomainEventPublisher = mock()
    private val useCase = RecordUsageEvent(repository, eventPublisher)

    private val tenantId = TenantId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"))
    private val userId = UserId(UUID.fromString("11111111-2222-3333-4444-555555555555"))

    private val command = RecordUsageEventCommand(
        tenantId = tenantId,
        userId = userId,
        service = "compute",
        product = "vm",
        eventDateTime = OffsetDateTime.parse("2026-01-15T10:00:00Z"),
        idempotencyKey = IdempotencyKey("key-123"),
    )

    @Test
    fun `should return Recorded and publish UsageRecorded event when idempotency key is new`() {
        val savedEvent = UsageEvent(
            id = UUID.randomUUID(),
            tenantId = command.tenantId,
            userId = command.userId,
            service = command.service,
            product = command.product,
            eventDateTime = command.eventDateTime,
            idempotencyKey = command.idempotencyKey,
        )
        given(repository.existsByIdempotencyKey(command.idempotencyKey)).willReturn(false)
        given(repository.save(any())).willReturn(savedEvent)

        val result = useCase.execute(command)

        assertIs<RecordUsageEventResult.Recorded>(result)
        assertEquals(savedEvent, result.event)
    }

    @Test
    fun `should publish UsageRecorded event with correct fields after saving`() {
        val savedEvent = UsageEvent(
            id = UUID.randomUUID(),
            tenantId = command.tenantId,
            userId = command.userId,
            service = command.service,
            product = command.product,
            eventDateTime = command.eventDateTime,
            idempotencyKey = command.idempotencyKey,
        )
        given(repository.existsByIdempotencyKey(command.idempotencyKey)).willReturn(false)
        given(repository.save(any())).willReturn(savedEvent)

        useCase.execute(command)

        val captor = argumentCaptor<UsageRecorded>()
        verify(eventPublisher).publish(captor.capture())
        val publishedEvent = captor.firstValue
        assertEquals(savedEvent.id, publishedEvent.usageEventId)
        assertEquals(savedEvent.tenantId, publishedEvent.tenantId)
        assertEquals(savedEvent.userId, publishedEvent.userId)
        assertEquals(savedEvent.service, publishedEvent.service)
        assertEquals(savedEvent.product, publishedEvent.product)
        assertEquals(savedEvent.idempotencyKey, publishedEvent.idempotencyKey)
        assertEquals(savedEvent.eventDateTime.toInstant(), publishedEvent.occurredAt)
    }

    @Test
    fun `should pass tenantId and userId from command to the saved UsageEvent`() {
        given(repository.existsByIdempotencyKey(command.idempotencyKey)).willReturn(false)
        given(repository.save(any())).willAnswer { invocation -> invocation.getArgument(0) as UsageEvent }

        val result = useCase.execute(command)

        assertIs<RecordUsageEventResult.Recorded>(result)
        assertEquals(tenantId, result.event.tenantId)
        assertEquals(userId, result.event.userId)
    }

    @Test
    fun `should return Duplicate and not save or publish when idempotency key already exists`() {
        given(repository.existsByIdempotencyKey(command.idempotencyKey)).willReturn(true)

        val result = useCase.execute(command)

        assertIs<RecordUsageEventResult.Duplicate>(result)
        verify(repository, never()).save(any())
        verify(eventPublisher, never()).publish(any())
    }

    @Test
    fun `should return Duplicate and not publish when concurrent save is rejected by database`() {
        given(repository.existsByIdempotencyKey(command.idempotencyKey)).willReturn(false)
        given(repository.save(any())).willThrow(DuplicateIdempotencyKeyException(command.idempotencyKey))

        val result = useCase.execute(command)

        assertIs<RecordUsageEventResult.Duplicate>(result)
        verify(eventPublisher, never()).publish(any())
    }
}
