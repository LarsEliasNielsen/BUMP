package cloud.larn.bump.infrastructure

import cloud.larn.bump.infrastructure.security.BumpJwtAuthenticationConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableMethodSecurity
class SecurityConfig(private val jwtDecoder: JwtDecoder) {

    // Developer tooling.
    // In a real deployment these paths would not be exposed: springdoc is disabled by default
    // (springdoc.swagger-ui.enabled=false / springdoc.api-docs.enabled=false in application.yaml)
    // and enabled only via application-local.yaml, so this chain never matches in production.
    @Bean
    @Order(1)
    fun developerToolsFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .securityMatcher("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
            .authorizeHttpRequests { it.anyRequest().permitAll() }
            .csrf { it.disable() }
            .build()

    // Customer-facing API.
    @Bean
    @Order(2)
    fun customerApiFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .securityMatcher("/**")     // Catch-all for requests not claimed by previous chain.
            .authorizeHttpRequests {
                it.requestMatchers("/").permitAll()
                it.requestMatchers(HttpMethod.POST, "/accounts", "/auth/login").permitAll()
                it.anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.decoder(jwtDecoder)
                    jwt.jwtAuthenticationConverter(BumpJwtAuthenticationConverter())
                }
            }
            .csrf { it.disable() }
            .build()

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
