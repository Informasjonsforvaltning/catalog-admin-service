package no.digdir.catalog_admin_service.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.digdir.catalog_admin_service.model.CodeList
import no.digdir.catalog_admin_service.model.CodeLists
import no.digdir.catalog_admin_service.utils.ApiTestContext
import no.digdir.catalog_admin_service.utils.CODE_LIST_0
import no.digdir.catalog_admin_service.utils.apiGet
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import kotlin.test.assertEquals

private val mapper = jacksonObjectMapper()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    properties = ["spring.profiles.active=contract-test"],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [ApiTestContext.Initializer::class])
@Tag("integration")
class CodeListTest: ApiTestContext() {

    @Test
    fun findCodeLists() {
        val response = apiGet(port,"/code-lists", null)
        assertTrue(HttpStatus.OK.value() == response["status"])
        val result: CodeLists = mapper.readValue(response["body"] as String)
        val expected = CodeLists(codeLists = listOf(CODE_LIST_0))
        assertEquals(expected, result)
    }
    @Test
    fun findCodeListById() {
        val response = apiGet(port,"/code-lists/123", null)
        assertTrue(HttpStatus.OK.value() == response["status"])
        val result: CodeList = mapper.readValue(response["body"] as String)
        assertEquals(CODE_LIST_0, result)
    }
    @Test
    fun codeListNotFound() {
        val response = apiGet(port,"/code-lists/xxx", null)
        assertTrue(HttpStatus.NOT_FOUND.value() == response["status"])
    }
}
