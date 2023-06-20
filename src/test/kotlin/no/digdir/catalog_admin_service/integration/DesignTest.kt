package no.digdir.catalog_admin_service.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

import no.digdir.catalog_admin_service.model.DesignDTO
import no.digdir.catalog_admin_service.utils.ApiTestContext

import no.digdir.catalog_admin_service.utils.DESIGN_DTO
import no.digdir.catalog_admin_service.utils.apiAuthorizedRequest
import no.digdir.catalog_admin_service.utils.apiGet
import no.digdir.catalog_admin_service.utils.jwk.Access
import no.digdir.catalog_admin_service.utils.jwk.JwtToken
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
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
class DesignTest : ApiTestContext(
) {
    @Nested
    internal inner class Design {
        private val path = "/910244132/design"

        @Test
        fun findDesign() {
            val response = apiAuthorizedRequest(
                path,
                port,
                null,
                JwtToken(Access.ORG_WRITE).toString(),
                HttpMethod.GET
            )
            assertEquals(HttpStatus.OK.value(), response["status"])
            val result: DesignDTO = mapper.readValue(response["body"] as String)
            assertEquals(DESIGN_DTO, result)
        }

        /* @Test
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
        fun createCodeList() {
            val path = "/910244132/concepts/code-lists"

            val before = apiAuthorizedRequest(
                path,
                port,
                null,
                JwtToken(Access.ORG_ADMIN).toString(),
                HttpMethod.GET
            )
            assertEquals(HttpStatus.OK.value(), before["status"])

            val createResponse = apiAuthorizedRequest(
                path,
                port,
                mapper.writeValueAsString(CODE_LIST_TO_BE_CREATED_0),
                JwtToken(Access.ORG_ADMIN).toString(),
                HttpMethod.POST
            )
            assertEquals(HttpStatus.CREATED.value(), createResponse["status"])

            val after = apiAuthorizedRequest(
                path,
                port,
                null,
                JwtToken(Access.ORG_ADMIN).toString(),
                HttpMethod.GET
            )
            assertEquals(HttpStatus.OK.value(), after["status"])

            val beforeList: CodeLists = mapper.readValue(before["body"] as String)
            val afterList: CodeLists = mapper.readValue(after["body"] as String)
            assertEquals(beforeList.codeLists.size + 1, afterList.codeLists.size)
        }
    }*/

    }
}
