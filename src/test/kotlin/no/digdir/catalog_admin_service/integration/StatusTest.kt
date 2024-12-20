package no.digdir.catalog_admin_service.integration


import no.digdir.catalog_admin_service.utils.ApiTestContext
import no.digdir.catalog_admin_service.utils.apiGet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    properties = ["spring.profiles.active=integration-test"],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ContextConfiguration(initializers = [ApiTestContext.Initializer::class])
@Tag("integration")
class StatusTest: ApiTestContext() {

    @Test
    fun ping() {
        val response = apiGet(port, "/ping", null)
        assertEquals(HttpStatus.OK.value(), response["status"])
    }

    @Test
    fun ready() {
        val response = apiGet(port, "/ready", null)
        assertEquals(HttpStatus.OK.value(), response["status"])
    }
}