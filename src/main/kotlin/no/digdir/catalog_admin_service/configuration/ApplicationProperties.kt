package no.digdir.catalog_admin_service.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("application")
data class ApplicationProperties(
    val adminServiceUri: String
)
