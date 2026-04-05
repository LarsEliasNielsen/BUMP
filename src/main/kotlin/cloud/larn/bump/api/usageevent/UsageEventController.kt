package cloud.larn.bump.api.usageevent

import cloud.larn.bump.api.ErrorResponse
import cloud.larn.bump.application.usecase.RecordUsageEvent
import cloud.larn.bump.application.usecase.RecordUsageEventCommand
import cloud.larn.bump.application.usecase.RecordUsageEventResult
import cloud.larn.bump.domain.model.IdempotencyKey
import cloud.larn.bump.domain.model.TenantId
import cloud.larn.bump.domain.model.UsageEvent
import cloud.larn.bump.domain.model.UserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "Usage Events", description = "Idempotent ingestion of raw usage events")
@RestController
@RequestMapping("/usage-events")
class UsageEventController(private val useCase: RecordUsageEvent) {

    @Operation(
        summary = "Record a usage event",
        description = "Records a usage event scoped to the authenticated tenant. " +
                "The `tenantId` and `userId` are extracted from the JWT Bearer token — callers cannot supply or spoof them. " +
                "Submitting the same `idempotencyKey` more than once returns 409 Conflict instead of recording a duplicate — safe to retry on network failure.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(
            responseCode = "201",
            description = "Usage event recorded",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = UsageEventResponse::class))]),
        ApiResponse(
            responseCode = "400",
            description = "Request body is not valid, or the JWT is missing required claims (tenantId, sub)",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(
            responseCode = "401",
            description = "Missing or invalid JWT Bearer token",
            content = [Content(mediaType = "application/json")]),
        ApiResponse(
            responseCode = "409",
            description = "Usage event with this idempotency key already exists",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    )
    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateUsageEventRequest,
        authentication: Authentication,
    ): ResponseEntity<UsageEventResponse> {
        val jwt = authentication as JwtAuthenticationToken
        val tenantIdStr = jwt.token.getClaimAsString("tenantId")
            ?: throw InvalidTokenClaimException()
        val tenantId = try {
            TenantId(UUID.fromString(tenantIdStr))
        } catch (_: IllegalArgumentException) {
            throw InvalidTokenClaimException()
        }
        val userId = try {
            UserId(UUID.fromString(jwt.token.subject))
        } catch (_: IllegalArgumentException) {
            throw InvalidTokenClaimException()
        }

        val command = RecordUsageEventCommand(
            tenantId = tenantId,
            userId = userId,
            service = request.service,
            product = request.product,
            eventDateTime = request.eventDateTime,
            idempotencyKey = IdempotencyKey(request.idempotencyKey),
        )
        return when (val result = useCase.execute(command)) {
            is RecordUsageEventResult.Recorded -> ResponseEntity.status(HttpStatus.CREATED).body(result.event.toResponse())
            is RecordUsageEventResult.Duplicate -> throw DuplicateUsageEventException()
        }
    }

    @Suppress("unused")
    @ExceptionHandler(DuplicateUsageEventException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleDuplicateUsageEvent(): ErrorResponse =
        ErrorResponse(error = "Usage event with this idempotency key already exists")

    @Suppress("unused")
    @ExceptionHandler(InvalidTokenClaimException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidTokenClaim(): ErrorResponse =
        ErrorResponse(error = "Authentication token is missing required claims")

    private fun UsageEvent.toResponse() = UsageEventResponse(
        id = id,
        tenantId = tenantId.value,
        userId = userId.value,
        service = service,
        product = product,
        eventDateTime = eventDateTime,
        idempotencyKey = idempotencyKey.value,
    )
}

private class DuplicateUsageEventException : RuntimeException()
private class InvalidTokenClaimException : RuntimeException()
