package cloud.larn.bump.api.auth

import cloud.larn.bump.api.ErrorResponse
import cloud.larn.bump.application.usecase.AuthenticateUser
import cloud.larn.bump.application.usecase.AuthenticateUserCommand
import cloud.larn.bump.application.usecase.AuthenticateUserResult
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Authentication", description = "JWT issuance")
@RestController
@RequestMapping("/auth")
class AuthController(private val useCase: AuthenticateUser) {

    @Operation(
        summary = "Log in and receive a JWT token",
        description = "Verifies the provided credentials and issues a signed JWT (HMAC-SHA256). " +
                "Include the token as 'Authorization: Bearer <token>' on all subsequent API calls. " +
                "Always returns 401 for bad credentials — the response never distinguishes between " +
                "an unknown email and a wrong password.")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Credentials accepted — JWT token returned",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = TokenResponse::class))]),
        ApiResponse(
            responseCode = "400",
            description = "Request is not valid (e.g. blank email or password)",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(
            responseCode = "401",
            description = "Credentials are incorrect",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    )
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<TokenResponse> =
        when (val result = useCase.execute(AuthenticateUserCommand(request.email, request.password))) {
            is AuthenticateUserResult.Authenticated -> ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(TokenResponse(
                    token = result.token,
                    expiresAt = result.expiresAt.toString(),
                ))
            AuthenticateUserResult.InvalidCredentials -> throw InvalidCredentialsException()
        }

    @Suppress("unused")
    @ExceptionHandler(InvalidCredentialsException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleInvalidCredentials(): ErrorResponse =
        ErrorResponse(error = "Invalid credentials")
}

private class InvalidCredentialsException : RuntimeException()
