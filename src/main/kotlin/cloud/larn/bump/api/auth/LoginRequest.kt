package cloud.larn.bump.api.auth

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import kotlinx.serialization.Serializable

@Serializable
@Schema(description = "Credentials for authenticating a registered user")
data class LoginRequest(
    @field:NotBlank
    @Schema(
        description = "Email address of the registered user",
        example = "alice@acme.com")
    val email: String,

    @field:NotBlank
    @Schema(
        description = "Password of the registered user",
        example = "s3cr3tP@ss")
    val password: String,
)
