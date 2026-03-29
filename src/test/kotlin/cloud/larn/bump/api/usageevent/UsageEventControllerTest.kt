package cloud.larn.bump.api.usageevent

import cloud.larn.bump.application.usageevent.UsageEventService
import cloud.larn.bump.domain.model.UsageEvent
import cloud.larn.bump.infrastructure.SecurityConfig
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
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
    private lateinit var service: UsageEventService

    @Test
    fun `POST usage-events returns 201 with response body`() {
        val id = UUID.randomUUID()
        val eventDateTime = OffsetDateTime.parse("2026-01-15T10:00:00Z")
        given(service.create(any(), any(), any(), any()))
            .willReturn(
                UsageEvent(
                    id = id,
                    userId = "user-123",
                    service = "compute",
                    product = "vm",
                    eventDateTime = eventDateTime,
                )
            )

        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"userId":"user-123","service":"compute","product":"vm","eventDateTime":"2026-01-15T10:00:00Z"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value(id.toString()) }
            jsonPath("$.userId") { value("user-123") }
            jsonPath("$.service") { value("compute") }
            jsonPath("$.product") { value("vm") }
        }
    }

    @Test
    fun `POST usage-events returns 400 when userId is blank`() {
        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"userId":"","service":"compute","product":"vm","eventDateTime":"2026-01-15T10:00:00Z"}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }

    @Test
    fun `POST usage-events returns 400 when service is blank`() {
        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"userId":"user-123","service":"","product":"vm","eventDateTime":"2026-01-15T10:00:00Z"}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }

    @Test
    fun `POST usage-events returns 400 when product is blank`() {
        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"userId":"user-123","service":"compute","product":"","eventDateTime":"2026-01-15T10:00:00Z"}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }

    @Test
    fun `POST usage-events returns 400 when service is missing`() {
        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"userId":"user-123","product":"vm","eventDateTime":"2026-01-15T10:00:00Z"}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }

    @Test
    fun `POST usage-events returns 400 when eventDateTime is missing`() {
        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"userId":"user-123","service":"compute","product":"vm"}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }

    @Test
    fun `POST usage-events returns 400 when eventDateTime has invalid format`() {
        mockMvc.post("/usage-events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"userId":"user-123","service":"compute","product":"vm","eventDateTime":"not-a-date"}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("Request is not valid") }
        }
    }
}
