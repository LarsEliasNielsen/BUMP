package cloud.larn.bump.application.usecase

import cloud.larn.bump.application.port.PasswordHasher
import cloud.larn.bump.domain.model.Email
import cloud.larn.bump.domain.model.Role
import cloud.larn.bump.domain.model.Tenant
import cloud.larn.bump.domain.model.TenantId
import cloud.larn.bump.domain.model.User
import cloud.larn.bump.domain.model.UserId
import cloud.larn.bump.domain.repository.TenantRepository
import cloud.larn.bump.domain.repository.UserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RegisterTenantTest {

    private val tenantRepository: TenantRepository = mock()
    private val userRepository: UserRepository = mock()
    private val passwordHasher: PasswordHasher = mock()
    private val useCase = RegisterTenant(tenantRepository, userRepository, passwordHasher)

    private val command = RegisterTenantCommand(
        companyName = "Acme Corp",
        adminEmail = "alice@acme.com",
        adminPassword = "s3cr3tP@ss",
    )

    @Test
    fun `should return Registered with tenantId and adminUserId when registration succeeds`() {
        val savedTenant = Tenant(id = TenantId(UUID.randomUUID()), name = command.companyName)
        val savedUser = User(
            id = UserId(UUID.randomUUID()),
            tenantId = savedTenant.id,
            email = Email(command.adminEmail),
            passwordHash = "hashed",
            role = Role.ADMIN,
        )
        given(userRepository.existsByEmail(Email(command.adminEmail))).willReturn(false)
        given(tenantRepository.save(any())).willReturn(savedTenant)
        given(userRepository.save(any())).willReturn(savedUser)
        given(passwordHasher.hash(command.adminPassword)).willReturn("hashed")

        val result = useCase.execute(command)

        assertIs<RegisterTenantResult.Registered>(result)
        assertEquals(savedTenant.id, result.tenantId)
        assertEquals(savedUser.id, result.adminUserId)
    }

    @Test
    fun `should return EmailAlreadyInUse when email is already registered`() {
        given(userRepository.existsByEmail(Email(command.adminEmail))).willReturn(true)

        val result = useCase.execute(command)

        assertIs<RegisterTenantResult.EmailAlreadyInUse>(result)
        verify(tenantRepository, never()).save(any())
        verify(userRepository, never()).save(any())
    }

    @Test
    fun `should hash password before saving user`() {
        val savedTenant = Tenant(id = TenantId(UUID.randomUUID()), name = command.companyName)
        given(userRepository.existsByEmail(any())).willReturn(false)
        given(tenantRepository.save(any())).willReturn(savedTenant)
        given(passwordHasher.hash(command.adminPassword)).willReturn("bcrypt-hash")
        given(userRepository.save(any())).willAnswer { it.arguments[0] as User }

        useCase.execute(command)

        val userCaptor = argumentCaptor<User>()
        verify(userRepository).save(userCaptor.capture())
        assertEquals("bcrypt-hash", userCaptor.firstValue.passwordHash)
    }

    @Test
    fun `should assign ADMIN role to the first user`() {
        val savedTenant = Tenant(id = TenantId(UUID.randomUUID()), name = command.companyName)
        given(userRepository.existsByEmail(any())).willReturn(false)
        given(tenantRepository.save(any())).willReturn(savedTenant)
        given(passwordHasher.hash(any())).willReturn("hashed")
        given(userRepository.save(any())).willAnswer { it.arguments[0] as User }

        useCase.execute(command)

        val userCaptor = argumentCaptor<User>()
        verify(userRepository).save(userCaptor.capture())
        assertEquals(Role.ADMIN, userCaptor.firstValue.role)
    }

    @Test
    fun `should throw IllegalArgumentException when adminEmail is not a valid email address`() {
        assertThrows<IllegalArgumentException> {
            useCase.execute(command.copy(adminEmail = "not-an-email"))
        }
    }

    @Test
    fun `should link user to the saved tenant`() {
        val savedTenant = Tenant(id = TenantId(UUID.randomUUID()), name = command.companyName)
        given(userRepository.existsByEmail(any())).willReturn(false)
        given(tenantRepository.save(any())).willReturn(savedTenant)
        given(passwordHasher.hash(any())).willReturn("hashed")
        given(userRepository.save(any())).willAnswer { it.arguments[0] as User }

        useCase.execute(command)

        val userCaptor = argumentCaptor<User>()
        verify(userRepository).save(userCaptor.capture())
        assertEquals(savedTenant.id, userCaptor.firstValue.tenantId)
    }
}
