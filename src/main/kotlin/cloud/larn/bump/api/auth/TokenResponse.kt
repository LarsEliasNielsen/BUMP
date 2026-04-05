package cloud.larn.bump.api.auth

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable

@Serializable
@Schema(description = "JWT token issued after successful authentication")
data class TokenResponse(
    @Schema(
        description = "Signed JWT to include as a Bearer token in subsequent API requests",
        example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.signature")
    val token: String,

    @Schema(
        description = "ISO-8601 timestamp at which the token expires",
        example = "2026-05-01T12:00:00Z")
    val expiresAt: String,
)
