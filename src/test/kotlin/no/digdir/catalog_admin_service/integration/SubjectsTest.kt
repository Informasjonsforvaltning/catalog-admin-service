package no.digdir.catalog_admin_service.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.digdir.catalog_admin_service.model.CodeList
import no.digdir.catalog_admin_service.utils.ApiTestContext
import no.digdir.catalog_admin_service.utils.CODE_LIST_0
import no.digdir.catalog_admin_service.utils.CODE_LIST_1
import no.digdir.catalog_admin_service.utils.TestResponseReader
import no.digdir.catalog_admin_service.utils.apiAuthorizedRequest
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import java.io.StringReader
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    properties = ["spring.profiles.active=integration-test"],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ContextConfiguration(initializers = [ApiTestContext.Initializer::class])
@Tag("integration")
class SubjectsTest : ApiTestContext() {

    val testResponseReader = TestResponseReader()
    private val mapper = jacksonObjectMapper()

    @Nested
    internal inner class RDF {

        @Test
        fun getAllConceptSubjects() {
            val response = apiAuthorizedRequest(
                "/concept-subjects",
                port,
                null,
                null,
                HttpMethod.GET,
                accept = MediaType.parseMediaType("text/turtle")
            )
            assertEquals(HttpStatus.OK.value(), response["status"])
            val model = ModelFactory.createDefaultModel().read(StringReader(response["body"] as String), "", "TURTLE")
            val expected = testResponseReader.parseTurtleFile("all-concept-subjects.ttl")
            assertTrue(model.isIsomorphicWith(expected))
        }

        @Test
        fun getCatalogSubjects() {
            val response = apiAuthorizedRequest(
                "/123456789/concepts/code-list/subjects",
                port,
                null,
                null,
                HttpMethod.GET,
                accept = MediaType.parseMediaType("text/turtle")
            )
            assertEquals(HttpStatus.OK.value(), response["status"])
            val model = ModelFactory.createDefaultModel().read(StringReader(response["body"] as String), "", "TURTLE")
            val expected = testResponseReader.parseTurtleFile("concept-subjects-for-single-catalog.ttl")
            assertTrue(model.isIsomorphicWith(expected))
        }

        @Test
        fun getCatalogSubjectsNotFoundWhenMissingInDB() {
            val response = apiAuthorizedRequest(
                "/111222333/concepts/code-list/subjects",
                port,
                null,
                null,
                HttpMethod.GET,
                accept = MediaType.parseMediaType("text/turtle")
            )
            assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
        }
    }

    @Nested
    internal inner class JSON {

        @Test
        fun getAllConceptSubjects() {
            val response = apiAuthorizedRequest(
                "/concept-subjects",
                port,
                null,
                null,
                HttpMethod.GET,
                accept = MediaType.parseMediaType("application/json")
            )
            assertEquals(HttpStatus.OK.value(), response["status"])
            val result: List<CodeList> = mapper.readValue(response["body"] as String)
            val expected = listOf(CODE_LIST_0, CODE_LIST_1)
            assertEquals(expected, result)
        }

        @Test
        fun getCatalogSubjects() {
            val response = apiAuthorizedRequest(
                "/123456789/concepts/code-list/subjects",
                port,
                null,
                null,
                HttpMethod.GET,
                accept = MediaType.parseMediaType("application/json")
            )
            assertEquals(HttpStatus.OK.value(), response["status"])
            val result: CodeList = mapper.readValue(response["body"] as String)
            assertEquals(CODE_LIST_1, result)
        }

        @Test
        fun getCatalogSubjectsNotFoundWhenMissingInDB() {
            val response = apiAuthorizedRequest(
                "/111222333/concepts/code-list/subjects",
                port,
                null,
                null,
                HttpMethod.GET,
                accept = MediaType.parseMediaType("application/json")
            )
            assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
        }
    }

}
