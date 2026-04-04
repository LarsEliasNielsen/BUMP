package cloud.larn.bump.domain.repository

import cloud.larn.bump.domain.model.Tenant

interface TenantRepository {
    fun save(tenant: Tenant): Tenant
}
