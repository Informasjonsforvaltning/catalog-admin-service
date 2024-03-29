package no.digdir.catalog_admin_service.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.digdir.catalog_admin_service.model.CodeList
import no.digdir.catalog_admin_service.model.CodeLists
import no.digdir.catalog_admin_service.model.JsonPatchOperation
import no.digdir.catalog_admin_service.model.OpEnum
import no.digdir.catalog_admin_service.utils.ApiTestContext
import no.digdir.catalog_admin_service.utils.CODE
import no.digdir.catalog_admin_service.utils.CODE_LIST_0
import no.digdir.catalog_admin_service.utils.CODE_LIST_2
import no.digdir.catalog_admin_service.utils.CODE_LIST_3
import no.digdir.catalog_admin_service.utils.NAME
import no.digdir.catalog_admin_service.utils.CODE_LIST_TO_BE_CREATED_0
import no.digdir.catalog_admin_service.utils.LIST_OF_CODE_LISTS_TO_BE_CREATED
import no.digdir.catalog_admin_service.utils.apiAuthorizedRequest
import no.digdir.catalog_admin_service.utils.apiGet
import no.digdir.catalog_admin_service.utils.jwk.Access
import no.digdir.catalog_admin_service.utils.jwk.JwtToken
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


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
            HttpMethod.GET
        )
        assertEquals(HttpStatus.OK.value(), response["status"])
        val result: CodeLists = mapper.readValue(response["body"] as String)
        val expected = CodeLists(codeLists = listOf(CODE_LIST_2, CODE_LIST_3, CODE_LIST_0))
        assertEquals(expected, result)
    }

    @Test
    fun findCodeListById() {
        val response = apiAuthorizedRequest(
            "/910244132/concepts/code-lists/123",
            port,
            null,
            JwtToken(Access.ORG_READ).toString(),
            HttpMethod.GET
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
            HttpMethod.GET
        )
        assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
    }

    @Test
    fun findCodeListsForbiddenForWrongOrg() {
        val response = apiAuthorizedRequest(
            "/910244132/concepts/code-lists",
            port,
            null,
            JwtToken(Access.WRONG_ORG_ADMIN).toString(),
            HttpMethod.GET
        )
        assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
    }

    @Test
    fun findCodeListByIdForbiddenForWrongOrg() {
        val response = apiAuthorizedRequest(
            "/910244132/concepts/code-lists/123",
            port,
            null,
            JwtToken(Access.WRONG_ORG_ADMIN).toString(),
            HttpMethod.GET
        )
        assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
    }

    @Test
    fun findCodeListByIdNotFoundForCodeListNotInCatalog() {
        val response = apiAuthorizedRequest(
            "/123456789/concepts/code-lists/123",
            port,
            null,
            JwtToken(Access.WRONG_ORG_ADMIN).toString(),
            HttpMethod.GET
        )
        assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
    }

    @Test
    fun deleteCodeList() {
        val path = "/910244132/concepts/code-lists/456"

        val preResponse = apiAuthorizedRequest(
            path,
            port,
            null,
            JwtToken(Access.ORG_ADMIN).toString(),
            HttpMethod.GET
        )
        assertEquals(HttpStatus.OK.value(), preResponse["status"])

        val deleteResponse = apiAuthorizedRequest(path, port, null, JwtToken(Access.ORG_ADMIN).toString(), HttpMethod.DELETE)
        assertEquals(HttpStatus.NO_CONTENT.value(), deleteResponse["status"])

        val postResponse = apiAuthorizedRequest(
            path,
            port,
            null,
            JwtToken(Access.ORG_ADMIN).toString(),
            HttpMethod.DELETE
        )
        assertEquals(HttpStatus.NOT_FOUND.value(), postResponse["status"])
    }

    @Test
    fun badRequestWhenDeletingCodeListInUseEditableField() {
        val path = "/910244132/concepts/code-lists/123"
        val deleteResponse = apiAuthorizedRequest(path, port, null, JwtToken(Access.ORG_ADMIN).toString(), HttpMethod.DELETE)
        assertEquals(HttpStatus.BAD_REQUEST.value(), deleteResponse["status"])
    }

    @Test
    fun badRequestWhenDeletingCodeListInUseInternalField() {
        val path = "/910244132/concepts/code-lists/678"
        val deleteResponse = apiAuthorizedRequest(path, port, null, JwtToken(Access.ORG_ADMIN).toString(), HttpMethod.DELETE)
        assertEquals(HttpStatus.BAD_REQUEST.value(), deleteResponse["status"])
    }

    @Test
    fun deleteCodeListWrongOrg() {
        val path = "/123456789/concepts/code-lists/123"
        val preResponse = apiAuthorizedRequest(
            path,
            port,
            null,
            JwtToken(Access.ORG_ADMIN).toString(),
            HttpMethod.DELETE
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
            HttpMethod.DELETE
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
            HttpMethod.DELETE
        )
        assertEquals(HttpStatus.NOT_FOUND.value(), preResponse["status"])
    }

    @Test
    fun createCodeList() {
        val path = "/910244132/concepts/code-lists"
        val createResponse = apiAuthorizedRequest(
            path,
            port,
            mapper.writeValueAsString(CODE_LIST_TO_BE_CREATED_0),
            JwtToken(Access.ORG_ADMIN).toString(),
            HttpMethod.POST
        )
        assertEquals(HttpStatus.CREATED.value(), createResponse["status"])

        val responseHeaders: HttpHeaders = createResponse["header"] as HttpHeaders
        val location = responseHeaders.location
        assertNotNull(location)

        val getResponse = apiAuthorizedRequest(location.toString(), port, null, JwtToken(Access.ORG_READ).toString(), HttpMethod.GET)
        assertEquals(HttpStatus.OK.value(), getResponse["status"])
        val result: CodeList = mapper.readValue(getResponse["body"] as String)
        val expected = CodeList(
            id = result.id,
            catalogId = "910244132",
            name = CODE_LIST_TO_BE_CREATED_0.name,
            description = CODE_LIST_TO_BE_CREATED_0.description,
            codes = CODE_LIST_TO_BE_CREATED_0.codes
        )
        assertEquals(expected, result)
    }

    @Test
    fun importThreeCodeLists() {
        val path = "/910244132/concepts/code-lists/import"
        val createResponse = apiAuthorizedRequest(
            path,
            port,
            mapper.writeValueAsString(LIST_OF_CODE_LISTS_TO_BE_CREATED),
            JwtToken(Access.ORG_ADMIN).toString(),
            HttpMethod.POST
        )
        assertEquals(HttpStatus.CREATED.value(), createResponse["status"])

        val getResponse = apiAuthorizedRequest( "/910244132/concepts/code-lists", port, null, JwtToken(Access.ORG_READ).toString(), HttpMethod.GET)
        assertEquals(HttpStatus.OK.value(), getResponse["status"])
        val result: CodeLists = mapper.readValue(getResponse["body"] as String)
        assertEquals(6, result.codeLists.size)
    }

    @Test
    fun importCodeListsForbiddenForWrongOrg() {
        val response = apiAuthorizedRequest(
            "/910244132/concepts/code-lists/import",
            port,
            mapper.writeValueAsString(LIST_OF_CODE_LISTS_TO_BE_CREATED),
            JwtToken(Access.WRONG_ORG_ADMIN).toString(),
            HttpMethod.POST
        )
        assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
    }

    @Test
    fun importCodeListsForbiddenForReadOnly() {
        val response = apiAuthorizedRequest(
            "/910244132/concepts/code-lists/import",
            port,
            mapper.writeValueAsString(LIST_OF_CODE_LISTS_TO_BE_CREATED),
            JwtToken(Access.ORG_READ).toString(),
            HttpMethod.POST
        )
        assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
    }


    @Nested
    internal inner class Update {

        @Test
        fun updateCodeListsUnauthorizedWhenMissingJwt() {
            val response = apiGet(port, "/910244132/concepts/code-lists", null)
            assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
        }

        @Test
        fun updateCodeListsForbiddenForReadOnly() {
            val operations = listOf(JsonPatchOperation(op = OpEnum.REPLACE, "/codes/0/name/en", "req"))
            val response = apiAuthorizedRequest(
                "/910244132/concepts/code-lists/123",
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.ORG_READ).toString(),
                HttpMethod.PATCH
            )
            assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
        }

        @Test
        fun updateCodeListForbiddenForWrongOrg() {
            val operations = listOf(JsonPatchOperation(op = OpEnum.REPLACE, "/codes/0/name/en", "req"))
            val response = apiAuthorizedRequest(
                "/910244132/concepts/code-lists/123",
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.WRONG_ORG_ADMIN).toString(),
                HttpMethod.PATCH
            )
            assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
        }

        @Test
        fun updateCodeList() {
            val operations = listOf(JsonPatchOperation(op = OpEnum.REPLACE, "/codes/0/name/en", "Updated name"))
            val response = apiAuthorizedRequest(
                "/910244132/concepts/code-lists/123",
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.ORG_ADMIN).toString(),
                HttpMethod.PATCH
            )

            assertEquals(HttpStatus.OK.value(), response["status"])

            val result: CodeList = mapper.readValue(response["body"] as String)
            assertEquals(
                CODE_LIST_0.copy(
                    codes = listOf(
                        CODE.copy(name = NAME.copy(en = "Updated name"))
                    )
                ), result
            )
        }

        @Test
        fun addNewCodeNameToCodeList() {
            val operations = listOf(JsonPatchOperation(op = OpEnum.ADD, "/codes/0/name/nn", "New name"))
            val response = apiAuthorizedRequest(
                "/910244132/concepts/code-lists/123",
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.ORG_ADMIN).toString(),
                HttpMethod.PATCH
            )
            assertEquals(HttpStatus.OK.value(), response["status"])

            val result: CodeList = mapper.readValue(response["body"] as String)
            assertEquals(
                CODE_LIST_0.copy(
                    codes = listOf(
                        CODE.copy(name = NAME.copy(nn = "New name"))
                    )
                ), result
            )

        }

        @Test
        fun updateCodeListNotFound() {
            val operations = listOf(JsonPatchOperation(op = OpEnum.REPLACE, "/description", "Changed description"))
            val response = apiAuthorizedRequest(
                "/910244132/concepts/code-lists/xxx",
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.ORG_ADMIN).toString(),
                HttpMethod.PATCH
            )
            assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
        }

        @Test
        fun updateCodeListWithCopy() {
            val operations = listOf(JsonPatchOperation(op = OpEnum.COPY, path = "/description", from = "/name"))
            val response = apiAuthorizedRequest(
                "/910244132/concepts/code-lists/123",
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.ORG_ADMIN).toString(),
                HttpMethod.PATCH
            )
            assertEquals(HttpStatus.OK.value(), response["status"])

            val result: CodeList = mapper.readValue(response["body"] as String)
            assertEquals(result.name, result.description)
            assertEquals(
                CODE_LIST_0.copy(
                    description = "name"
                ), result
            )
        }

        @Test
        fun updateCodeListRemove() {
            val operations = listOf(JsonPatchOperation(op = OpEnum.REMOVE, path = "/codes/0/name/en"))
            val response = apiAuthorizedRequest(
                "/910244132/concepts/code-lists/123",
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.ORG_ADMIN).toString(),
                HttpMethod.PATCH
            )
            assertEquals(HttpStatus.OK.value(), response["status"])

            val result: CodeList = mapper.readValue(response["body"] as String)
            assertEquals(
                CODE_LIST_0.copy(
                    codes = listOf(
                        CODE.copy(name = NAME.copy(en = null))
                    )
                ), result
            )
        }

        @Test
        fun cannotRemoveRequiredValue() {
            val operations = listOf(JsonPatchOperation(op = OpEnum.REMOVE, path = "name"))
            val response = apiAuthorizedRequest(
                "/910244132/concepts/code-lists/123",
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.ORG_ADMIN).toString(),
                HttpMethod.PATCH
            )
            assertEquals(HttpStatus.BAD_REQUEST.value(), response["status"])
        }

        @Test
        fun updateCodeListMove() {
            val operations = listOf(JsonPatchOperation(op = OpEnum.MOVE, path = "/name", from = "/codes/0/name/en"))
            val response = apiAuthorizedRequest(
                "/910244132/concepts/code-lists/123",
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.ORG_ADMIN).toString(),
                HttpMethod.PATCH
            )

            assertEquals(HttpStatus.OK.value(), response["status"])

            val result: CodeList = mapper.readValue(response["body"] as String)
            assertEquals(
                CODE_LIST_0.copy(
                    name = CODE_LIST_0.codes[0].name.en!!, codes = listOf(
                        CODE.copy(name = NAME.copy(en = null))
                    )
                ), result
            )
        }

        @Test
        fun badRequestWhenInvalidValue() {
            val operations = listOf(JsonPatchOperation(op = OpEnum.REPLACE, path = "/codes", value = "1234"))
            val response = apiAuthorizedRequest(
                "/910244132/concepts/code-lists/123",
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.ORG_ADMIN).toString(),
                HttpMethod.PATCH
            )

            assertEquals(HttpStatus.BAD_REQUEST.value(), response["status"])
        }

        @Test
        fun badRequestWhenUpdatingId() {
            val operations = listOf(JsonPatchOperation(op = OpEnum.REPLACE, path = "/id", value = "1234"))
            val response = apiAuthorizedRequest(
                "/910244132/concepts/code-lists/123",
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.ORG_ADMIN).toString(),
                HttpMethod.PATCH
            )

            assertEquals(HttpStatus.BAD_REQUEST.value(), response["status"])
        }

        @Test
        fun badRequestWhenUpdatingCatalogId() {
            val operations = listOf(JsonPatchOperation(op = OpEnum.REPLACE, path = "/catalogId", value = "1234"))
            val response = apiAuthorizedRequest(
                "/910244132/concepts/code-lists/123",
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.ORG_ADMIN).toString(),
                HttpMethod.PATCH
            )

            assertEquals(HttpStatus.BAD_REQUEST.value(), response["status"])
        }
    }
}
