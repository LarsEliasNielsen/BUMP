package cloud.larn.bump.domain.usageevent

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "usage_events")
class UsageEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID? = null,

    @Column(name = "user_id", nullable = false)
    val userId: String,

    @Column(name = "service", nullable = false)
    val service: String,

    @Column(name = "product", nullable = false)
    val product: String,

    @Column(name = "event_date_time", nullable = false)
    val eventDateTime: OffsetDateTime,
)
