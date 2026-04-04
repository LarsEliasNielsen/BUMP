package cloud.larn.bump.infrastructure.persistence

import cloud.larn.bump.domain.model.Email
import cloud.larn.bump.domain.model.TenantId
import cloud.larn.bump.domain.model.User
import cloud.larn.bump.domain.model.UserId
import cloud.larn.bump.domain.repository.UserRepository
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryAdapter(
    private val jpa: UserJpaRepository,
) : UserRepository {

    override fun save(user: User): User =
        jpa.save(user.toEntity()).toDomain()

    override fun existsByEmail(email: Email): Boolean =
        jpa.existsByEmail(email.value)

    override fun findByEmail(email: Email): User? =
        jpa.findByEmail(email.value)?.toDomain()

    private fun User.toEntity() = UserEntity(
        id = id.value,
        tenantId = tenantId.value,
        email = email.value,
        passwordHash = passwordHash,
        role = role,
    )

    private fun UserEntity.toDomain() = User(
        id = UserId(id),
        tenantId = TenantId(tenantId),
        email = Email(email),
        passwordHash = passwordHash,
        role = role,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )
}
