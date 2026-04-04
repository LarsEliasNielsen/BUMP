package cloud.larn.bump.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class AuditableEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()

    // Null when the action is system-initiated (e.g. self-service tenant registration has no actor yet).
    // Populated from the JWT principal once authentication is in place.
    @CreatedBy
    @Column(name = "created_by_id", updatable = false)
    var createdById: UUID? = null

    @LastModifiedBy
    @Column(name = "updated_by_id")
    var updatedById: UUID? = null

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null

    @Column(name = "deleted_by_id")
    var deletedById: UUID? = null
}
