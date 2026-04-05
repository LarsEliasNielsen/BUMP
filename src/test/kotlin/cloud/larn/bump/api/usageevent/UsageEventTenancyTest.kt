package cloud.larn.bump.api.usageevent

import cloud.larn.bump.infrastructure.persistence.TenantJpaRepository
import cloud.larn.bump.infrastructure.persistence.UserJpaRepository
import cloud.larn.bump.infrastructure.persistence.UsageEventJpaRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class UsageEventTenancyTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jwtEncoder: JwtEncoder

    @Autowired
    private lateinit var usageEventJpaRepository: UsageEventJpaRepository

    @Autowired
    private lateinit var userJpaRepository: UserJpaRepository

    @Autowired
    private lateinit var tenantJpaRepository: TenantJpaRepository

    @BeforeEach
    @AfterEach
    fun cleanup() {
        // Delete in FK order: usage_events → users → tenants
        usageEventJpaRepository.deleteAll()
        userJpaRepository.deleteAll()
        tenantJpaRepository.deleteAll()
    }

    @Test
    fun `POST usage-events persists event with tenantId and userId from JWT`() {
        // Arrange: register a tenant and user so the FK constraints are satisfied
        val (registeredTenantId, registeredUserId) = registerTenantAndUser()

        val token = mintJwt(sub = registeredUserId, tenantId = registeredTenantId)

        // Act
        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"service":"compute","product":"vm","eventDateTime":"2026-01-15T10:00:00Z","idempotencyKey":"tenancy-test-key-${UUID.randomUUID()}"}"""
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isCreated() }
            jsonPath("$.tenantId") { value(registeredTenantId.toString()) }
            jsonPath("$.userId") { value(registeredUserId.toString()) }
        }

        // Assert: verify the persisted row has the correct FK values
        val events = usageEventJpaRepository.findAll()
        assertEquals(1, events.size)
        val stored = events.first()
        assertEquals(registeredTenantId, stored.tenantId)
        assertEquals(registeredUserId, stored.userId)
    }

    // --- helpers ---

    private fun mintJwt(sub: UUID, tenantId: UUID): String {
        val claims = JwtClaimsSet.builder()
            .subject(sub.toString())
            .claim("tenantId", tenantId.toString())
            .claim("role", "ADMIN")
            .issuedAt(Instant.now().minusSeconds(60))
            .expiresAt(Instant.now().plusSeconds(3600))
            .id(UUID.randomUUID().toString())
            .build()
        return jwtEncoder.encode(
            JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)
        ).tokenValue
    }

    private fun registerTenantAndUser(): Pair<UUID, UUID> {
        val email = "tenancy-test-${UUID.randomUUID()}@example.com"
        val responseBody = mockMvc.post("/accounts") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"companyName":"Tenancy Test Corp","adminEmail":"$email","adminPassword":"s3cr3t!X9"}"""
        }.andExpect {
            status { isCreated() }
        }.andReturn().response.contentAsString

        // Extract tenantId and userId from the registration response via simple regex
        val tenantIdMatch = Regex(""""tenantId"\s*:\s*"([^"]+)"""").find(responseBody)
        requireNotNull(tenantIdMatch) { "tenantId not found in registration response: $responseBody" }
        val userIdMatch = Regex(""""adminUserId"\s*:\s*"([^"]+)"""").find(responseBody)
        requireNotNull(userIdMatch) { "adminUserId not found in registration response: $responseBody" }
        return UUID.fromString(tenantIdMatch.groupValues[1]) to UUID.fromString(userIdMatch.groupValues[1])
    }
}
