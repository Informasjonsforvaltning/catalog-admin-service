package no.digdir.catalog_admin_service.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jwt.*
import org.springframework.security.oauth2.jwt.JwtClaimNames.AUD
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration

@Configuration
open class SecurityConfig(
    @Value("\${application.cors.originPatterns}")
    val corsOriginPatterns: Array<String>
) {

    @Bean
    open fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { cors ->
                cors.configurationSource { _ ->
                    val config = CorsConfiguration()
                    config.allowCredentials = false
                    config.allowedHeaders = listOf("*")
                    config.maxAge = 3600L
                    config.allowedOriginPatterns = corsOriginPatterns.toList()
                    config.allowedMethods = listOf("GET", "POST", "OPTIONS", "DELETE", "PATCH")
                    config
                }
            }
            .authorizeHttpRequests { authorize ->
                authorize.requestMatchers(HttpMethod.OPTIONS).permitAll()
                    .requestMatchers(HttpMethod.GET, "/ping").permitAll()
                    .requestMatchers(HttpMethod.GET, "/ready").permitAll()
                    .requestMatchers(HttpMethod.GET, "/concept-subjects").permitAll()
                    .requestMatchers(HttpMethod.GET, "/*/concepts/code-list/subjects").permitAll()
                    .anyRequest().authenticated() }
            .oauth2ResourceServer { resourceServer -> resourceServer.jwt() }
        return http.build()
    }

    @Bean
    open fun jwtDecoder(properties: OAuth2ResourceServerProperties): JwtDecoder {
        val jwtDecoder = NimbusJwtDecoder.withJwkSetUri(properties.jwt.jwkSetUri).build()
        jwtDecoder.setJwtValidator(
            DelegatingOAuth2TokenValidator(
                JwtTimestampValidator(),
                JwtIssuerValidator(properties.jwt.issuerUri),
                JwtClaimValidator(AUD) { aud: List<String> -> aud.contains("catalog-admin-service") }
            )
        )
        return jwtDecoder
    }

}