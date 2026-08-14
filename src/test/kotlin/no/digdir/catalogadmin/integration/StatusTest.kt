package no.digdir.catalogadmin.integration

import no.digdir.catalogadmin.utils.ApiTestContext
import no.digdir.catalogadmin.utils.apiGet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.testcontainers.context.ImportTestcontainers
import org.springframework.http.HttpStatus

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    properties = ["spring.profiles.active=integration-test"],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@ImportTestcontainers(ApiTestContext::class)
@Tag("integration")
class StatusTest : ApiTestContext() {
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
