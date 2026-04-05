package cloud.larn.bump.application.usecase

import cloud.larn.bump.application.port.IssuedToken
import cloud.larn.bump.application.port.PasswordHasher
import cloud.larn.bump.application.port.TokenIssuer
import cloud.larn.bump.domain.model.Email
import cloud.larn.bump.domain.model.Role
import cloud.larn.bump.domain.model.TenantId
import cloud.larn.bump.domain.model.User
import cloud.larn.bump.domain.model.UserId
import cloud.larn.bump.domain.repository.UserRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AuthenticateUserTest {

    private val userRepository: UserRepository = mock()
    private val passwordHasher: PasswordHasher = mock()
    private val tokenIssuer: TokenIssuer = mock()
    private val useCase = AuthenticateUser(userRepository, passwordHasher, tokenIssuer)

    private val tenantId = TenantId(UUID.randomUUID())
    private val userId = UserId(UUID.randomUUID())
    private val storedUser = User(
        id = userId,
        tenantId = tenantId,
        email = Email("alice@acme.com"),
        passwordHash = "hashed-password",
        role = Role.ADMIN,
    )
    private val issuedToken = IssuedToken(token = "jwt.token.value", expiresAt = Instant.now().plusSeconds(86400))

    @Test
    fun `should return Authenticated with token when credentials are valid`() {
        given(userRepository.findByEmail(Email("alice@acme.com"))).willReturn(storedUser)
        given(passwordHasher.verify("s3cr3tP@ss", "hashed-password")).willReturn(true)
        given(tokenIssuer.issue(userId, tenantId, Role.ADMIN)).willReturn(issuedToken)

        val result = useCase.execute(AuthenticateUserCommand("alice@acme.com", "s3cr3tP@ss"))

        assertIs<AuthenticateUserResult.Authenticated>(result)
        assertEquals(issuedToken.token, result.token)
        assertEquals(issuedToken.expiresAt, result.expiresAt)
    }

    @Test
    fun `should return InvalidCredentials when email is not registered`() {
        given(userRepository.findByEmail(Email("unknown@acme.com"))).willReturn(null)

        val result = useCase.execute(AuthenticateUserCommand("unknown@acme.com", "s3cr3tP@ss"))

        assertIs<AuthenticateUserResult.InvalidCredentials>(result)
        verify(tokenIssuer, never()).issue(any(), any(), any())
    }

    @Test
    fun `should return InvalidCredentials when password is wrong`() {
        given(userRepository.findByEmail(Email("alice@acme.com"))).willReturn(storedUser)
        given(passwordHasher.verify("wrong-password", "hashed-password")).willReturn(false)

        val result = useCase.execute(AuthenticateUserCommand("alice@acme.com", "wrong-password"))

        assertIs<AuthenticateUserResult.InvalidCredentials>(result)
        verify(tokenIssuer, never()).issue(any(), any(), any())
    }

    @Test
    fun `should return InvalidCredentials when email has invalid format`() {
        val result = useCase.execute(AuthenticateUserCommand("not-an-email", "s3cr3tP@ss"))

        assertIs<AuthenticateUserResult.InvalidCredentials>(result)
        verify(userRepository, never()).findByEmail(any())
        verify(tokenIssuer, never()).issue(any(), any(), any())
    }

    @Test
    fun `should return the same error for unknown email and wrong password`() {
        given(userRepository.findByEmail(Email("alice@acme.com"))).willReturn(null)
        val unknownResult = useCase.execute(AuthenticateUserCommand("alice@acme.com", "s3cr3tP@ss"))

        given(userRepository.findByEmail(Email("alice@acme.com"))).willReturn(storedUser)
        given(passwordHasher.verify("wrong-password", "hashed-password")).willReturn(false)
        val wrongPasswordResult = useCase.execute(AuthenticateUserCommand("alice@acme.com", "wrong-password"))

        assertIs<AuthenticateUserResult.InvalidCredentials>(unknownResult)
        assertIs<AuthenticateUserResult.InvalidCredentials>(wrongPasswordResult)
    }
}
