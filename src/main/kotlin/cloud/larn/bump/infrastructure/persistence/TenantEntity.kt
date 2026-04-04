package cloud.larn.bump.infrastructure.persistence

import cloud.larn.bump.domain.model.TenantStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "tenants")
class TenantEntity(
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    var id: UUID,

    @Column(name = "name", nullable = false)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: TenantStatus,
) : AuditableEntity()
