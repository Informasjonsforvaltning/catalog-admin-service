package no.digdir.catalog_admin_service.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.digdir.catalog_admin_service.model.CodeList
import no.digdir.catalog_admin_service.model.CodeLists
import no.digdir.catalog_admin_service.utils.ApiTestContext
import no.digdir.catalog_admin_service.utils.CODE_LIST_0
import no.digdir.catalog_admin_service.utils.CODE_LIST_TO_BE_CREATED_0
import no.digdir.catalog_admin_service.utils.apiAuthorizedRequest
import no.digdir.catalog_admin_service.utils.apiGet
import no.digdir.catalog_admin_service.utils.jwk.Access
import no.digdir.catalog_admin_service.utils.jwk.JwtToken
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
    properties = ["spring.profiles.active=integration-test"],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ContextConfiguration(initializers = [ApiTestContext.Initializer::class])
@Tag("integration")
class CodeListTest : ApiTestContext() {

    @Test
    fun findCodeLists() {
        val response = apiAuthorizedRequest(
            "/910244132/concepts/code-lists",
            port,
            null,
            JwtToken(Access.ORG_WRITE).toString(),
            "GET"
        )
        assertEquals(HttpStatus.OK.value(), response["status"])
        val result: CodeLists = mapper.readValue(response["body"] as String)
        val expected = CodeLists(codeLists = listOf(CODE_LIST_0))
        assertEquals(expected, result)
    }

    @Test
    fun findCodeListById() {
        val response = apiAuthorizedRequest(
            "/910244132/concepts/code-lists/123",
            port,
            null,
            JwtToken(Access.ORG_READ).toString(),
            "GET"
        )
        assertEquals(HttpStatus.OK.value(), response["status"])
        val result: CodeList = mapper.readValue(response["body"] as String)
        assertEquals(CODE_LIST_0, result)
    }

    @Test
    fun findCodeListsUnauthorizedWhenMissingJwt() {
        val response = apiGet(port, "/910244132/concepts/code-lists", null)
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
    }

    @Test
    fun findCodeListByIdUnauthorizedWhenMissingJwt() {
        val response = apiGet(port, "/910244132/concepts/code-lists/123", null)
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
    }

    @Test
    fun codeListNotFound() {
        val response = apiAuthorizedRequest(
            "/910244132/concepts/code-lists/xxx",
            port,
            null,
            JwtToken(Access.ROOT).toString(),
            "GET"
        )
        assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
    }

    @Test
    fun findCodeListsForbiddenForWrongOrg() {
        val response = apiAuthorizedRequest(
            "/910244132/concepts/code-lists",
            port,
            null,
            JwtToken(Access.WRONG_ORG_READ).toString(),
            "GET"
        )
        assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
    }

    @Test
    fun findCodeListByIdForbiddenForWrongOrg() {
        val response = apiAuthorizedRequest(
            "/910244132/concepts/code-lists/123",
            port,
            null,
            JwtToken(Access.WRONG_ORG_READ).toString(),
            "GET"
        )
        assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
    }

    @Test
    fun findCodeListByIdNotFoundForCodeListNotInCatalog() {
        val response = apiAuthorizedRequest(
            "/123456789/concepts/code-lists/123",
            port,
            null,
            JwtToken(Access.WRONG_ORG_READ).toString(),
            "GET"
        )
        assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
    }

    @Test
    fun deleteCodeList() {
        val path = "/910244132/concepts/code-lists/123"

        val preResponse = apiAuthorizedRequest(
            path,
            port,
            null,
            JwtToken(Access.ORG_ADMIN).toString(),
            "GET"
        )
        assertEquals(HttpStatus.OK.value(), preResponse["status"])

        val deleteResponse = apiAuthorizedRequest(path, port, null, JwtToken(Access.ORG_ADMIN).toString(), "DELETE")
        assertEquals(HttpStatus.NO_CONTENT.value(), deleteResponse["status"])

        val postResponse = apiAuthorizedRequest(
            path,
            port,
            null,
            JwtToken(Access.ORG_ADMIN).toString(),
            "GET"
        )
        assertEquals(HttpStatus.NOT_FOUND.value(), postResponse["status"])
    }

    @Test
    fun deleteCodeListWrongOrd() {
        val path = "/123456789/concepts/code-lists/123"
        val preResponse = apiAuthorizedRequest(
            path,
            port,
            null,
            JwtToken(Access.ORG_ADMIN).toString(),
            "DELETE"
        )
        assertEquals(HttpStatus.FORBIDDEN.value(), preResponse["status"])
    }

    @Test
    fun deleteCodeListReadOnly() {
        val path = "/910244132/concepts/code-lists/123"
        val preResponse = apiAuthorizedRequest(
            path,
            port,
            null,
            JwtToken(Access.ORG_READ).toString(),
            "DELETE"
        )
        assertEquals(HttpStatus.FORBIDDEN.value(), preResponse["status"])
    }

    @Test
    fun deleteCodeListThatDoesNotExist() {
        val path = "/910244132/concepts/code-lists/xxx"
        val preResponse = apiAuthorizedRequest(
            path,
            port,
            null,
            JwtToken(Access.ORG_ADMIN).toString(),
            "DELETE"
        )
        assertEquals(HttpStatus.NOT_FOUND.value(), preResponse["status"])
    }

    @Test
    fun createCodeList() {
        val path = "/910244132/concepts/code-lists"

        val before = apiAuthorizedRequest(
            path,
            port,
            null,
            JwtToken(Access.ORG_ADMIN).toString(),
            "GET"
        )
        assertEquals(HttpStatus.OK.value(), before["status"])

        val createResponse = apiAuthorizedRequest(
            path,
            port,
            mapper.writeValueAsString(CODE_LIST_TO_BE_CREATED_0),
            JwtToken(Access.ORG_ADMIN).toString(),
            "POST"
        )
        assertEquals(HttpStatus.CREATED.value(), createResponse["status"])


        val after = apiAuthorizedRequest(
            path,
            port,
            null,
            JwtToken(Access.ORG_ADMIN).toString(),
            "GET"
        )
        assertEquals(HttpStatus.OK.value(), after["status"])


        val beforeList: CodeLists = mapper.readValue(before["body"] as String)
        val afterList: CodeLists = mapper.readValue(after["body"] as String)
        assertEquals(beforeList.codeLists.size + 1, afterList.codeLists.size)
    }
}
