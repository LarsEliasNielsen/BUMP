package cloud.larn.bump.api.usageevent

import cloud.larn.bump.application.usecase.RecordUsageEvent
import cloud.larn.bump.application.usecase.RecordUsageEventCommand
import cloud.larn.bump.application.usecase.RecordUsageEventResult
import cloud.larn.bump.domain.model.IdempotencyKey
import cloud.larn.bump.domain.model.TenantId
import cloud.larn.bump.domain.model.UsageEvent
import cloud.larn.bump.domain.model.UserId
import cloud.larn.bump.infrastructure.SecurityConfig
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.assertEquals

@WebMvcTest(UsageEventController::class)
@Import(SecurityConfig::class)
class UsageEventControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var useCase: RecordUsageEvent

    // SecurityConfig wires an OAuth2 resource server that requires a JwtDecoder bean.
    // The decoder is mocked here because these tests exercise controller logic, not JWT validation.
    // JWT validation is covered by RouteProtectionTest.
    @MockitoBean
    private lateinit var jwtDecoder: JwtDecoder

    private val tenantId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")
    private val userId = UUID.fromString("11111111-2222-3333-4444-555555555555")

    @Test
    fun `POST usage-events returns 201 with response body`() {
        val id = UUID.randomUUID()
        val eventDateTime = OffsetDateTime.parse("2026-01-15T10:00:00Z")
        given(useCase.execute(any()))
            .willReturn(
                RecordUsageEventResult.Recorded(
                    UsageEvent(
                        id = id,
                        tenantId = TenantId(tenantId),
                        userId = UserId(userId),
                        service = "compute",
                        product = "vm",
                        eventDateTime = eventDateTime,
                        idempotencyKey = IdempotencyKey("idempotency-key-123"),
                    )
                )
            )

        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"service":"compute","product":"vm","eventDateTime":"2026-01-15T10:00:00Z","idempotencyKey":"idempotency-key-123"}"""
            with(jwt().jwt { it.subject(userId.toString()).claim("tenantId", tenantId.toString()) })
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value(id.toString()) }
            jsonPath("$.tenantId") { value(tenantId.toString()) }
            jsonPath("$.userId") { value(userId.toString()) }
            jsonPath("$.service") { value("compute") }
            jsonPath("$.product") { value("vm") }
            jsonPath("$.idempotencyKey") { value("idempotency-key-123") }
        }
    }

    @Test
    fun `POST usage-events uses tenantId and userId from JWT claims`() {
        given(useCase.execute(any()))
            .willReturn(
                RecordUsageEventResult.Recorded(
                    UsageEvent(
                        tenantId = TenantId(tenantId),
                        userId = UserId(userId),
                        service = "compute",
                        product = "vm",
                        eventDateTime = OffsetDateTime.parse("2026-01-15T10:00:00Z"),
                        idempotencyKey = IdempotencyKey("key-123"),
                    )
                )
            )

        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"service":"compute","product":"vm","eventDateTime":"2026-01-15T10:00:00Z","idempotencyKey":"key-123"}"""
            with(jwt().jwt { it.subject(userId.toString()).claim("tenantId", tenantId.toString()) })
        }.andExpect {
            status { isCreated() }
        }

        val captor = argumentCaptor<RecordUsageEventCommand>()
        verify(useCase).execute(captor.capture())
        assertEquals(TenantId(tenantId), captor.firstValue.tenantId)
        assertEquals(UserId(userId), captor.firstValue.userId)
    }

    @Test
    fun `POST usage-events returns 400 when JWT is missing tenantId claim`() {
        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"service":"compute","product":"vm","eventDateTime":"2026-01-15T10:00:00Z","idempotencyKey":"key-123"}"""
            with(jwt().jwt { it.subject(userId.toString()) /* no tenantId claim */ })
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Authentication token is missing required claims") }
        }
    }

    @Test
    fun `POST usage-events returns 409 when idempotency key already exists`() {
        given(useCase.execute(any())).willReturn(RecordUsageEventResult.Duplicate)

        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"service":"compute","product":"vm","eventDateTime":"2026-01-15T10:00:00Z","idempotencyKey":"duplicate-key"}"""
            with(jwt().jwt { it.subject(userId.toString()).claim("tenantId", tenantId.toString()) })
        }.andExpect {
            status { isConflict() }
            jsonPath("$.error") { value("Usage event with this idempotency key already exists") }
        }
    }

    @Test
    fun `POST usage-events returns 400 when service is blank`() {
        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"service":"","product":"vm","eventDateTime":"2026-01-15T10:00:00Z","idempotencyKey":"key-123"}"""
            with(jwt().jwt { it.subject(userId.toString()).claim("tenantId", tenantId.toString()) })
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }

    @Test
    fun `POST usage-events returns 400 when product is blank`() {
        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"service":"compute","product":"","eventDateTime":"2026-01-15T10:00:00Z","idempotencyKey":"key-123"}"""
            with(jwt().jwt { it.subject(userId.toString()).claim("tenantId", tenantId.toString()) })
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }

    @Test
    fun `POST usage-events returns 400 when service is missing`() {
        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"product":"vm","eventDateTime":"2026-01-15T10:00:00Z","idempotencyKey":"key-123"}"""
            with(jwt().jwt { it.subject(userId.toString()).claim("tenantId", tenantId.toString()) })
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }

    @Test
    fun `POST usage-events returns 400 when eventDateTime is missing`() {
        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"service":"compute","product":"vm","idempotencyKey":"key-123"}"""
            with(jwt().jwt { it.subject(userId.toString()).claim("tenantId", tenantId.toString()) })
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }

    @Test
    fun `POST usage-events returns 400 when eventDateTime has invalid format`() {
        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"service":"compute","product":"vm","eventDateTime":"not-a-date","idempotencyKey":"key-123"}"""
            with(jwt().jwt { it.subject(userId.toString()).claim("tenantId", tenantId.toString()) })
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }

    @Test
    fun `POST usage-events returns 400 when idempotencyKey is missing`() {
        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"service":"compute","product":"vm","eventDateTime":"2026-01-15T10:00:00Z"}"""
            with(jwt().jwt { it.subject(userId.toString()).claim("tenantId", tenantId.toString()) })
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }
}
