package cloud.larn.bump.infrastructure.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class BumpJwtAuthenticationConverter : Converter<Jwt, AbstractAuthenticationToken> {

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        // A missing role claim means the token was not issued by this service.
        // Grant no authorities so downstream access checks fail rather than
        // permitting access under an opaque fallback role.
        val role = jwt.getClaimAsString("role")
        val authorities = if (role != null) listOf(SimpleGrantedAuthority("ROLE_$role")) else emptyList()
        return JwtAuthenticationToken(jwt, authorities)
    }
}
