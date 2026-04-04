package cloud.larn.bump.infrastructure.persistence

import cloud.larn.bump.domain.model.Tenant
import cloud.larn.bump.domain.model.TenantId
import cloud.larn.bump.domain.repository.TenantRepository
import org.springframework.stereotype.Repository

@Repository
class TenantRepositoryAdapter(
    private val jpa: TenantJpaRepository,
) : TenantRepository {

    override fun save(tenant: Tenant): Tenant =
        jpa.save(tenant.toEntity()).toDomain()

    private fun Tenant.toEntity() = TenantEntity(
        id = id.value,
        name = name,
        status = status,
    )

    private fun TenantEntity.toDomain() = Tenant(
        id = TenantId(id),
        name = name,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )
}
