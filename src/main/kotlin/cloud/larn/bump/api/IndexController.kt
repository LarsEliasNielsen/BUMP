package cloud.larn.bump.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class IndexController {

    @GetMapping
    fun index(): Map<String, String> =
        mapOf("message" to "Welcome to the Billing and Usage Metering Platform (BUMP)!")
}
