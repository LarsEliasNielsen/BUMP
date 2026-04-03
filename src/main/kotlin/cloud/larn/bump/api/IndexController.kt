package cloud.larn.bump.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Platform", description = "Platform status")
@RestController
@RequestMapping("/")
class IndexController {

    @Operation(
        summary = "Platform welcome",
        description = "Returns a welcome message confirming the platform is reachable.")
    @GetMapping
    fun index(): Map<String, String> =
        mapOf("message" to "Welcome to the Billing and Usage Metering Platform (BUMP)!")
}
