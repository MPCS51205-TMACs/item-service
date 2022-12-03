package com.mpcs51205.itemservice.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver
import org.springframework.security.web.SecurityFilterChain
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.spec.SecretKeySpec

@Component
class SecurityFilter {
    @Value("\${jwt.secret}")
    lateinit var secret: String

    @Bean
    fun configure(http: HttpSecurity?): SecurityFilterChain {
        return http!!
            .csrf { it.disable() }
            .cors {}
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests { auth ->
              auth.antMatchers("/item/counterfeit/**", "/item/inappropriate/**").hasRole("ADMIN")
                  .antMatchers("/item/**").hasRole("USER")

                    .and()
                    .oauth2ResourceServer()
                    .bearerTokenResolver {
                        DefaultBearerTokenResolver().resolve(it)
                    }
                    .jwt().jwtAuthenticationConverter(TokenConverter())
            }
            .build()
    }

    @Bean
    fun jwtDecoder(): JwtDecoder? {
        return NimbusJwtDecoder.withSecretKey(SecretKeySpec(secret.toByteArray(), "HmacSHA256")).build()
    }

    class TokenConverter : Converter<Jwt, AbstractAuthenticationToken> {
        override fun convert(source: Jwt): AbstractAuthenticationToken? {
            val user = getUser(jwt = source)
            return UsernamePasswordAuthenticationToken(user, "", user.grantedAuthorities)
        }

        private fun getUser(jwt: Jwt) = AuthenticatedUser(getUserId(jwt), getEmail(jwt), getAuthorities(jwt))

        private fun getUserId(jwt: Jwt): UUID {
            return UUID.fromString(jwt.claims["sub"] as String)
        }

        private fun getEmail(jwt: Jwt): String {
            return jwt.claims["email"] as String
        }

        private fun getAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
            return jwt.claims.get("authorities").let { it as List<*> }
                .map { auth -> SimpleGrantedAuthority(auth.toString()) }
        }
    }


}

data class AuthenticatedUser(val userId: UUID, val email:String, val grantedAuthorities: Collection<GrantedAuthority>) :
    User(userId.toString(), "", grantedAuthorities)