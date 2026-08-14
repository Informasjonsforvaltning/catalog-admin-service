package no.digdir.catalog_admin_service.utils

import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import java.net.HttpURLConnection
import java.net.URL

abstract class ApiTestContext {
    @LocalServerPort
    var port = 0

    @BeforeEach
    fun resetDatabase() {
        resetDB()
    }

    companion object {
        @JvmStatic
        val postgresContainer: KPostgreSQLContainer =
            KPostgreSQLContainer("postgres:16")
                .withDatabaseName(DB_NAME)
                .withUsername(DB_USER)
                .withPassword(DB_PASSWORD)

        init {
            startMockServer()
            postgresContainer.start()

            resetDB()

            try {
                val con = URL("http://localhost:5050/ping").openConnection() as HttpURLConnection
                con.connect()
                if (con.responseCode != 200) {
                    stopMockServer()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                stopMockServer()
            }
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgresContainer.getJdbcUrl() }
            registry.add("spring.datasource.username") { DB_USER }
            registry.add("spring.datasource.password") { DB_PASSWORD }
        }
    }
}

class KPostgreSQLContainer(imageName: String) : PostgreSQLContainer<KPostgreSQLContainer>(imageName)
