package no.digdir.catalog_admin_service

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity

@EnableWebSecurity
@SpringBootApplication
open class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}