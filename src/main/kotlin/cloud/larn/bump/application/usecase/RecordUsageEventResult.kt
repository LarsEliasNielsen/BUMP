package cloud.larn.bump.application.usecase

import cloud.larn.bump.domain.model.UsageEvent

sealed class RecordUsageEventResult {
    data class Recorded(val event: UsageEvent) : RecordUsageEventResult()
    data object Duplicate : RecordUsageEventResult()
}
