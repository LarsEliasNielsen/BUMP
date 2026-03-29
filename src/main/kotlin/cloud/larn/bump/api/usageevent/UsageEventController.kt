package cloud.larn.bump.api.usageevent

import cloud.larn.bump.application.usecase.RecordUsageEvent
import cloud.larn.bump.application.usecase.RecordUsageEventCommand
import cloud.larn.bump.application.usecase.RecordUsageEventResult
import cloud.larn.bump.domain.model.CustomerId
import cloud.larn.bump.domain.model.IdempotencyKey
import cloud.larn.bump.domain.model.UsageEvent
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/usage-events")
class UsageEventController(private val useCase: RecordUsageEvent) {

    @PostMapping
    fun create(@Valid @RequestBody request: CreateUsageEventRequest): ResponseEntity<UsageEventResponse> {
        val command = RecordUsageEventCommand(
            customerId = CustomerId(request.customerId),
            service = request.service,
            product = request.product,
            eventDateTime = request.eventDateTime,
            idempotencyKey = IdempotencyKey(request.idempotencyKey),
        )
        return when (val result = useCase.execute(command)) {
            is RecordUsageEventResult.Recorded -> ResponseEntity.status(HttpStatus.CREATED).body(result.event.toResponse())
            is RecordUsageEventResult.Duplicate -> ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
    }

    private fun UsageEvent.toResponse() = UsageEventResponse(
        id = id,
        customerId = customerId.value,
        service = service,
        product = product,
        eventDateTime = eventDateTime,
        idempotencyKey = idempotencyKey.value,
    )
}
