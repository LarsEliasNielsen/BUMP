package cloud.larn.bump.api

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val error: String)
