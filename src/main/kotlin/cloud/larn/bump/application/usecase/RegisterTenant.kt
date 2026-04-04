package cloud.larn.bump.application.usecase

import cloud.larn.bump.application.port.PasswordHasher
import cloud.larn.bump.domain.model.Email
import cloud.larn.bump.domain.model.Role
import cloud.larn.bump.domain.model.Tenant
import cloud.larn.bump.domain.model.User
import cloud.larn.bump.domain.repository.TenantRepository
import cloud.larn.bump.domain.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RegisterTenant(
    private val tenantRepository: TenantRepository,
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
) {

    @Transactional
    fun execute(command: RegisterTenantCommand): RegisterTenantResult {
        val email = Email(command.adminEmail)

        if (userRepository.existsByEmail(email)) {
            return RegisterTenantResult.EmailAlreadyInUse
        }

        val tenant = tenantRepository.save(Tenant(name = command.companyName))
        val user = userRepository.save(
            User(
                tenantId = tenant.id,
                email = email,
                passwordHash = passwordHasher.hash(command.adminPassword),
                role = Role.ADMIN,
            )
        )

        return RegisterTenantResult.Registered(tenant.id, user.id)
    }
}
