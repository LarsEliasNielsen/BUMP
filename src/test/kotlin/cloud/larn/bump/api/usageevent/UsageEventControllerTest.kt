package cloud.larn.bump.api.usageevent

import cloud.larn.bump.application.usecase.RecordUsageEvent
import cloud.larn.bump.application.usecase.RecordUsageEventResult
import cloud.larn.bump.domain.model.CustomerId
import cloud.larn.bump.domain.model.IdempotencyKey
import cloud.larn.bump.domain.model.UsageEvent
import cloud.larn.bump.infrastructure.SecurityConfig
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
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
import java.util.*

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

    @Test
    fun `POST usage-events returns 201 with response body`() {
        val id = UUID.randomUUID()
        val eventDateTime = OffsetDateTime.parse("2026-01-15T10:00:00Z")
        given(useCase.execute(any()))
            .willReturn(
                RecordUsageEventResult.Recorded(
                    UsageEvent(
                        id = id,
                        customerId = CustomerId("customer-123"),
                        service = "compute",
                        product = "vm",
                        eventDateTime = eventDateTime,
                        idempotencyKey = IdempotencyKey("idempotency-key-123"),
                    )
                )
            )

        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"customerId":"customer-123","service":"compute","product":"vm","eventDateTime":"2026-01-15T10:00:00Z","idempotencyKey":"idempotency-key-123"}"""
            with(jwt())
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value(id.toString()) }
            jsonPath("$.customerId") { value("customer-123") }
            jsonPath("$.service") { value("compute") }
            jsonPath("$.product") { value("vm") }
            jsonPath("$.idempotencyKey") { value("idempotency-key-123") }
        }
    }

    @Test
    fun `POST usage-events returns 409 when idempotency key already exists`() {
        given(useCase.execute(any())).willReturn(RecordUsageEventResult.Duplicate)

        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"customerId":"customer-123","service":"compute","product":"vm","eventDateTime":"2026-01-15T10:00:00Z","idempotencyKey":"duplicate-key"}"""
            with(jwt())
        }.andExpect {
            status { isConflict() }
            jsonPath("$.error") { value("Usage event with this idempotency key already exists") }
        }
    }

    @Test
    fun `POST usage-events returns 400 when customerId is blank`() {
        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"customerId":"","service":"compute","product":"vm","eventDateTime":"2026-01-15T10:00:00Z","idempotencyKey":"key-123"}"""
            with(jwt())
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }

    @Test
    fun `POST usage-events returns 400 when service is blank`() {
        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"customerId":"customer-123","service":"","product":"vm","eventDateTime":"2026-01-15T10:00:00Z","idempotencyKey":"key-123"}"""
            with(jwt())
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }

    @Test
    fun `POST usage-events returns 400 when product is blank`() {
        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"customerId":"customer-123","service":"compute","product":"","eventDateTime":"2026-01-15T10:00:00Z","idempotencyKey":"key-123"}"""
            with(jwt())
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }

    @Test
    fun `POST usage-events returns 400 when service is missing`() {
        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"customerId":"customer-123","product":"vm","eventDateTime":"2026-01-15T10:00:00Z","idempotencyKey":"key-123"}"""
            with(jwt())
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }

    @Test
    fun `POST usage-events returns 400 when eventDateTime is missing`() {
        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"customerId":"customer-123","service":"compute","product":"vm","idempotencyKey":"key-123"}"""
            with(jwt())
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }

    @Test
    fun `POST usage-events returns 400 when eventDateTime has invalid format`() {
        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"customerId":"customer-123","service":"compute","product":"vm","eventDateTime":"not-a-date","idempotencyKey":"key-123"}"""
            with(jwt())
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }

    @Test
    fun `POST usage-events returns 400 when idempotencyKey is missing`() {
        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"customerId":"customer-123","service":"compute","product":"vm","eventDateTime":"2026-01-15T10:00:00Z"}"""
            with(jwt())
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }
}
