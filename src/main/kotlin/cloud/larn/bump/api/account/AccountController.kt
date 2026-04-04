package cloud.larn.bump.api.account

import cloud.larn.bump.api.ErrorResponse
import cloud.larn.bump.application.usecase.RegisterTenant
import cloud.larn.bump.application.usecase.RegisterTenantCommand
import cloud.larn.bump.application.usecase.RegisterTenantResult
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Accounts", description = "Tenant account registration")
@RestController
@RequestMapping("/accounts")
class AccountController(private val useCase: RegisterTenant) {

    @Operation(
        summary = "Register a company account",
        description = "Creates a new tenant account and its first admin user. The admin user can then log in " +
                "to receive a JWT token for authenticated API calls. Returns 409 Conflict if the email " +
                "address is already registered on the platform.")
    @ApiResponses(
        ApiResponse(
            responseCode = "201",
            description = "Account and admin user created successfully",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = AccountResponse::class))]),
        ApiResponse(
            responseCode = "400",
            description = "Request is not valid (e.g. missing fields, invalid email format, password too short)",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(
            responseCode = "409",
            description = "Email address is already registered",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    )
    @PostMapping
    fun register(@Valid @RequestBody request: RegisterAccountRequest): ResponseEntity<AccountResponse> {
        val command = RegisterTenantCommand(
            companyName = request.companyName,
            adminEmail = request.adminEmail,
            adminPassword = request.adminPassword,
        )
        return when (val result = useCase.execute(command)) {
            is RegisterTenantResult.Registered -> ResponseEntity.status(HttpStatus.CREATED).body(
                AccountResponse(
                    tenantId = result.tenantId.value.toString(),
                    adminUserId = result.adminUserId.value.toString(),
                )
            )
            is RegisterTenantResult.EmailAlreadyInUse -> throw EmailAlreadyInUseException()
        }
    }

    @Suppress("unused")
    @ExceptionHandler(EmailAlreadyInUseException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleEmailAlreadyInUse(): ErrorResponse =
        ErrorResponse(error = "Email address is already registered")
}

private class EmailAlreadyInUseException : RuntimeException()
