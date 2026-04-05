package cloud.larn.bump.application.usecase

import java.time.Instant

sealed class AuthenticateUserResult {
    data class Authenticated(val token: String, val expiresAt: Instant) : AuthenticateUserResult()
    data object InvalidCredentials : AuthenticateUserResult()
}
