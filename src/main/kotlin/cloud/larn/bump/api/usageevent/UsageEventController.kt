package cloud.larn.bump.api.usageevent

import cloud.larn.bump.application.usageevent.UsageEventService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/usage-events")
class UsageEventController(private val service: UsageEventService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateUsageEventRequest): UsageEventResponse {
        val saved = service.create(
            customerId = request.customerId,
            service = request.service,
            product = request.product,
            eventDateTime = request.eventDateTime,
        )
        return UsageEventResponse(
            id = saved.id,
            customerId = saved.customerId.value,
            service = saved.service,
            product = saved.product,
            eventDateTime = saved.eventDateTime,
        )
    }
}
