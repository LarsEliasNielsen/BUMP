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
    var id: UUID,

    @Column(name = "user_id", nullable = false)
    var userId: String,

    @Column(name = "service", nullable = false)
    var service: String,

    @Column(name = "product", nullable = false)
    var product: String,

    @Column(name = "event_date_time", nullable = false)
    var eventDateTime: OffsetDateTime,
)
