package cloud.larn.bump.application.usecase

data class RegisterTenantCommand(
    val companyName: String,
    val adminEmail: String,
    val adminPassword: String,
)
