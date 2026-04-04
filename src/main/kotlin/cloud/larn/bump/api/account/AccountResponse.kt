package cloud.larn.bump.api.account

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable

@Serializable
@Schema(description = "Response returned after successfully registering a company account")
data class AccountResponse(
    @Schema(
        description = "Unique identifier of the newly created tenant account",
        example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    val tenantId: String,

    @Schema(
        description = "Unique identifier of the initial admin user created for this account",
        example = "b2c3d4e5-f6a7-8901-bcde-f12345678901")
    val adminUserId: String,
)
