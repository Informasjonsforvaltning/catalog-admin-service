package no.digdir.catalogadmin.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("application")
data class ApplicationProperties(val adminServiceUri: String)
