package cloud.larn.bump.api.auth

import cloud.larn.bump.application.usecase.AuthenticateUser
import cloud.larn.bump.application.usecase.AuthenticateUserResult
import cloud.larn.bump.infrastructure.SecurityConfig
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.Instant

@WebMvcTest(AuthController::class)
@Import(SecurityConfig::class)
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var useCase: AuthenticateUser

    @Test
    fun `POST auth login returns 200 with token and Cache-Control no-store on valid credentials`() {
        val expiresAt = Instant.parse("2025-04-06T12:00:00Z")
        given(useCase.execute(any())).willReturn(
            AuthenticateUserResult.Authenticated(token = "jwt.token.value", expiresAt = expiresAt)
        )

        mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"alice@acme.com","password":"s3cr3tP@ss"}"""
        }.andExpect {
            status { isOk() }
            header { string(HttpHeaders.CACHE_CONTROL, "no-store") }
            jsonPath("$.token") { value("jwt.token.value") }
            jsonPath("$.expiresAt") { value(expiresAt.toString()) }
        }
    }

    @Test
    fun `POST auth login returns 401 when credentials are invalid`() {
        given(useCase.execute(any())).willReturn(AuthenticateUserResult.InvalidCredentials)

        mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"alice@acme.com","password":"wrong-password"}"""
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.error") { value("Invalid credentials") }
        }
    }

    @Test
    fun `POST auth login returns 400 when email is blank`() {
        mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"","password":"s3cr3tP@ss"}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }

    @Test
    fun `POST auth login returns 400 when password is blank`() {
        mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"alice@acme.com","password":""}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }

    @Test
    fun `POST auth login returns 400 when email is missing`() {
        mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"password":"s3cr3tP@ss"}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }

    @Test
    fun `POST auth login returns 400 when password is missing`() {
        mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"alice@acme.com"}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }
}
