package cloud.larn.bump.application.usecase

data class AuthenticateUserCommand(val email: String, val password: String)
