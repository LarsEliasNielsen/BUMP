package cloud.larn.bump.api.account

import cloud.larn.bump.application.usecase.RegisterTenant
import cloud.larn.bump.application.usecase.RegisterTenantResult
import cloud.larn.bump.domain.model.TenantId
import cloud.larn.bump.domain.model.UserId
import cloud.larn.bump.infrastructure.SecurityConfig
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.util.UUID

@WebMvcTest(AccountController::class)
@Import(SecurityConfig::class)
class AccountControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var useCase: RegisterTenant

    // SecurityConfig wires an OAuth2 resource server that requires a JwtDecoder bean.
    // POST /accounts is public, so the decoder is never invoked — mocked to satisfy wiring only.
    @MockitoBean
    private lateinit var jwtDecoder: JwtDecoder

    @Test
    fun `POST accounts returns 201 with tenantId and adminUserId`() {
        val tenantId = UUID.randomUUID()
        val adminUserId = UUID.randomUUID()
        given(useCase.execute(any())).willReturn(
            RegisterTenantResult.Registered(TenantId(tenantId), UserId(adminUserId))
        )

        mockMvc.post("/accounts") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"companyName":"Acme Corp","adminEmail":"alice@acme.com","adminPassword":"s3cr3tP@ss"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.tenantId") { value(tenantId.toString()) }
            jsonPath("$.adminUserId") { value(adminUserId.toString()) }
        }
    }

    @Test
    fun `POST accounts returns 409 when email is already registered`() {
        given(useCase.execute(any())).willReturn(RegisterTenantResult.EmailAlreadyInUse)

        mockMvc.post("/accounts") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"companyName":"Acme Corp","adminEmail":"alice@acme.com","adminPassword":"s3cr3tP@ss"}"""
        }.andExpect {
            status { isConflict() }
            jsonPath("$.error") { value("Email address is already registered") }
        }
    }

    @Test
    fun `POST accounts returns 400 when companyName is blank`() {
        mockMvc.post("/accounts") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"companyName":"","adminEmail":"alice@acme.com","adminPassword":"s3cr3tP@ss"}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }

    @Test
    fun `POST accounts returns 400 when adminEmail has invalid format`() {
        mockMvc.post("/accounts") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"companyName":"Acme Corp","adminEmail":"not-an-email","adminPassword":"s3cr3tP@ss"}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }

    @Test
    fun `POST accounts returns 400 when adminPassword is shorter than 8 characters`() {
        mockMvc.post("/accounts") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"companyName":"Acme Corp","adminEmail":"alice@acme.com","adminPassword":"short"}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }

    @Test
    fun `POST accounts returns 400 when companyName is missing`() {
        mockMvc.post("/accounts") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"adminEmail":"alice@acme.com","adminPassword":"s3cr3tP@ss"}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }

    @Test
    fun `POST accounts returns 400 when adminEmail is missing`() {
        mockMvc.post("/accounts") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"companyName":"Acme Corp","adminPassword":"s3cr3tP@ss"}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }

    @Test
    fun `POST accounts returns 400 when adminPassword is missing`() {
        mockMvc.post("/accounts") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"companyName":"Acme Corp","adminEmail":"alice@acme.com"}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }
}
