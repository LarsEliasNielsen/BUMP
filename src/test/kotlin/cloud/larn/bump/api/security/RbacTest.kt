package cloud.larn.bump.api.security

import cloud.larn.bump.api.usageevent.UsageEventController
import cloud.larn.bump.application.usecase.RecordUsageEvent
import cloud.larn.bump.application.usecase.RecordUsageEventResult
import cloud.larn.bump.domain.model.IdempotencyKey
import cloud.larn.bump.domain.model.TenantId
import cloud.larn.bump.domain.model.UsageEvent
import cloud.larn.bump.domain.model.UserId
import cloud.larn.bump.infrastructure.SecurityConfig
import cloud.larn.bump.infrastructure.security.JwtConfig
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID

@WebMvcTest(UsageEventController::class)
@Import(SecurityConfig::class, JwtConfig::class)
class RbacTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jwtEncoder: JwtEncoder

    @MockitoBean
    private lateinit var useCase: RecordUsageEvent

    @Test
    fun `DEVELOPER role can post usage events`() {
        stubRecordedResult()

        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = validUsageEventBody()
            header("Authorization", "Bearer ${jwtWithRole("DEVELOPER")}")
        }.andExpect {
            status { isCreated() }
        }
    }

    @Test
    fun `ADMIN role can post usage events`() {
        stubRecordedResult()

        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = validUsageEventBody()
            header("Authorization", "Bearer ${jwtWithRole("ADMIN")}")
        }.andExpect {
            status { isCreated() }
        }
    }

    @Test
    fun `PLATFORM_ADMIN role can post usage events`() {
        stubRecordedResult()

        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = validUsageEventBody()
            header("Authorization", "Bearer ${jwtWithRole("PLATFORM_ADMIN")}")
        }.andExpect {
            status { isCreated() }
        }
    }

    @Test
    fun `MANAGER role is forbidden from posting usage events`() {
        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = validUsageEventBody()
            header("Authorization", "Bearer ${jwtWithRole("MANAGER")}")
        }.andExpect {
            status { isForbidden() }
        }
    }

    // --- helpers ---

    private fun jwtWithRole(role: String): String {
        val claims = JwtClaimsSet.builder()
            .subject(UUID.randomUUID().toString())
            .claim("tenantId", UUID.randomUUID().toString())
            .claim("role", role)
            .issuedAt(Instant.now().minusSeconds(60))
            .expiresAt(Instant.now().plusSeconds(3600))
            .id(UUID.randomUUID().toString())
            .build()
        return jwtEncoder.encode(
            JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)
        ).tokenValue
    }

    private fun stubRecordedResult() {
        given(useCase.execute(any())).willReturn(
            RecordUsageEventResult.Recorded(
                UsageEvent(
                    id = UUID.randomUUID(),
                    tenantId = TenantId(UUID.randomUUID()),
                    userId = UserId(UUID.randomUUID()),
                    service = "compute",
                    product = "vm",
                    eventDateTime = OffsetDateTime.parse("2026-01-15T10:00:00Z"),
                    idempotencyKey = IdempotencyKey("key-123"),
                )
            )
        )
    }

    private fun validUsageEventBody() =
        """{"service":"compute","product":"vm","eventDateTime":"2026-01-15T10:00:00Z","idempotencyKey":"key-123"}"""
}
