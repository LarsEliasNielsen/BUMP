package cloud.larn.bump.api.account

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import kotlinx.serialization.Serializable

@Serializable
@Schema(description = "Request body for registering a new company account")
data class RegisterAccountRequest(
    @field:NotBlank
    @Schema(
        description = "Legal name of the company registering on the platform",
        example = "Acme Corp")
    val companyName: String,

    @field:NotBlank
    @field:Email
    @Schema(
        description = "Email address of the initial admin user for this account",
        example = "alice@acme.com")
    val adminEmail: String,

    // @NotBlank complements @Size(min=8): @Size accepts whitespace-only strings of sufficient length,
    // while @NotBlank rejects them. Both are needed to rule out all white-space passwords.
    @field:NotBlank
    @field:Size(min = 8)
    @Schema(
        description = "Password for the initial admin user. Minimum 8 characters.",
        example = "s3cr3tP@ss")
    val adminPassword: String,
)
