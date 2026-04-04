package cloud.larn.bump.application.usecase

import cloud.larn.bump.domain.model.TenantId
import cloud.larn.bump.domain.model.UserId

sealed class RegisterTenantResult {
    data class Registered(val tenantId: TenantId, val adminUserId: UserId) : RegisterTenantResult()
    data object EmailAlreadyInUse : RegisterTenantResult()
}
