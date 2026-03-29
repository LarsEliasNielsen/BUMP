package cloud.larn.bump.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "usage_events")
class UsageEventEntity(
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID,

    @Column(name = "user_id", nullable = false)
    val userId: String,

    @Column(name = "service", nullable = false)
    val service: String,

    @Column(name = "product", nullable = false)
    val product: String,

    @Column(name = "event_date_time", nullable = false)
    val eventDateTime: OffsetDateTime,
)
