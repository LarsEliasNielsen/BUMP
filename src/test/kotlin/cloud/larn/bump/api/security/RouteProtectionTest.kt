package cloud.larn.bump.api.security

import cloud.larn.bump.api.usageevent.UsageEventController
import cloud.larn.bump.application.usecase.RecordUsageEvent
import cloud.larn.bump.application.usecase.RecordUsageEventResult
import cloud.larn.bump.domain.model.CustomerId
import cloud.larn.bump.domain.model.IdempotencyKey
import cloud.larn.bump.domain.model.UsageEvent
import cloud.larn.bump.infrastructure.SecurityConfig
import cloud.larn.bump.infrastructure.security.JwtConfig
import com.nimbusds.jose.jwk.source.ImmutableSecret
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
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID
import javax.crypto.spec.SecretKeySpec

@WebMvcTest(UsageEventController::class)
@Import(SecurityConfig::class, JwtConfig::class)
class RouteProtectionTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    // Autowired from JwtConfig — used to mint real tokens that go through the real NimbusJwtDecoder.
    @Autowired
    private lateinit var jwtEncoder: JwtEncoder

    @MockitoBean
    private lateinit var useCase: RecordUsageEvent

    @Test
    fun `unauthenticated request to protected endpoint returns 401`() {
        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = validUsageEventBody()
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `request with valid JWT passes through decoder and reaches endpoint`() {
        val id = UUID.randomUUID()
        given(useCase.execute(any())).willReturn(
            RecordUsageEventResult.Recorded(
                UsageEvent(
                    id = id,
                    customerId = CustomerId("customer-123"),
                    service = "compute",
                    product = "vm",
                    eventDateTime = OffsetDateTime.parse("2026-01-15T10:00:00Z"),
                    idempotencyKey = IdempotencyKey("key-123"),
                )
            )
        )

        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = validUsageEventBody()
            header("Authorization", "Bearer ${validJwt()}")
        }.andExpect {
            status { isCreated() }
        }
    }

    @Test
    fun `request with expired JWT returns 401`() {
        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = validUsageEventBody()
            header("Authorization", "Bearer ${expiredJwt()}")
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `request with malformed JWT returns 401`() {
        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = validUsageEventBody()
            header("Authorization", "Bearer not.a.valid.jwt")
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `request with JWT signed by wrong key returns 401`() {
        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = validUsageEventBody()
            header("Authorization", "Bearer ${wrongKeyJwt()}")
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    // --- helpers ---

    private fun validJwt() = buildJwt(
        issuedAt = Instant.now().minusSeconds(60),
        expiresAt = Instant.now().plusSeconds(3600),
    )

    // NimbusJwtDecoder applies a default 60-second clock skew tolerance.
    // Use 1 hour ago so the token is unambiguously expired regardless of skew.
    private fun expiredJwt() = buildJwt(
        issuedAt = Instant.now().minusSeconds(7200),
        expiresAt = Instant.now().minusSeconds(3600),
    )

    private fun buildJwt(issuedAt: Instant, expiresAt: Instant): String {
        val claims = JwtClaimsSet.builder()
            .subject(UUID.randomUUID().toString())
            .claim("tenantId", UUID.randomUUID().toString())
            .claim("role", "ADMIN")
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .id(UUID.randomUUID().toString())
            .build()
        return jwtEncoder.encode(
            JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)
        ).tokenValue
    }

    private fun wrongKeyJwt(): String {
        // A structurally valid, unexpired JWT — but signed with a different 256-bit key.
        // The decoder will verify the signature against our configured key and reject it.
        val differentKey = SecretKeySpec(ByteArray(32) { 0x42 }, "HmacSHA256")
        val wrongEncoder = NimbusJwtEncoder(ImmutableSecret(differentKey))
        val claims = JwtClaimsSet.builder()
            .subject(UUID.randomUUID().toString())
            .claim("tenantId", UUID.randomUUID().toString())
            .claim("role", "ADMIN")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .id(UUID.randomUUID().toString())
            .build()
        return wrongEncoder.encode(
            JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)
        ).tokenValue
    }

    private fun validUsageEventBody() =
        """{"customerId":"customer-123","service":"compute","product":"vm","eventDateTime":"2026-01-15T10:00:00Z","idempotencyKey":"key-123"}"""
}
