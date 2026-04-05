package cloud.larn.bump.application.usecase

import cloud.larn.bump.application.port.PasswordHasher
import cloud.larn.bump.application.port.TokenIssuer
import cloud.larn.bump.domain.model.Email
import cloud.larn.bump.domain.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthenticateUser(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenIssuer: TokenIssuer,
) {

    @Transactional(readOnly = true)
    fun execute(command: AuthenticateUserCommand): AuthenticateUserResult {
        val email = try {
            Email(command.email)
        } catch (e: IllegalArgumentException) {
            return AuthenticateUserResult.InvalidCredentials
        }

        val user = userRepository.findByEmail(email)
            ?: return AuthenticateUserResult.InvalidCredentials

        if (!passwordHasher.verify(command.password, user.passwordHash)) {
            return AuthenticateUserResult.InvalidCredentials
        }

        val token = tokenIssuer.issue(user.id, user.tenantId, user.role)
        return AuthenticateUserResult.Authenticated(token.token, token.expiresAt)
    }
}
