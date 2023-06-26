package no.digdir.catalog_admin_service.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.digdir.catalog_admin_service.model.EditableFields
import no.digdir.catalog_admin_service.model.Fields
import no.digdir.catalog_admin_service.utils.ApiTestContext
import no.digdir.catalog_admin_service.utils.apiAuthorizedRequest
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
class FieldsTest : ApiTestContext() {

    @Nested
    internal inner class GetFields {
        private val path = "/910244132/concepts/fields"

        @Test
        fun ableToGetCatalogFieldsForAllOrgRoles() {
            val readRsp = apiAuthorizedRequest(path, port, null, JwtToken(Access.ORG_READ).toString(), HttpMethod.GET)
            val writeRsp = apiAuthorizedRequest(path, port, null, JwtToken(Access.ORG_WRITE).toString(), HttpMethod.GET)
            val adminRsp = apiAuthorizedRequest(path, port, null, JwtToken(Access.ORG_ADMIN).toString(), HttpMethod.GET)

            assertEquals(HttpStatus.OK.value(), readRsp["status"])
            assertEquals(HttpStatus.OK.value(), writeRsp["status"])
            assertEquals(HttpStatus.OK.value(), adminRsp["status"])

            val readResult: Fields = mapper.readValue(readRsp["body"] as String)
            val writeResult: Fields = mapper.readValue(writeRsp["body"] as String)
            val adminResult: Fields = mapper.readValue(adminRsp["body"] as String)

            val expected = Fields(editable = EditableFields("910244132", null), internal = emptyList())
            assertEquals(expected, readResult)
            assertEquals(expected, writeResult)
            assertEquals(expected, adminResult)
        }

        @Test
        fun forbiddenForWrongOrg() {
            val response = apiAuthorizedRequest(path, port, null, JwtToken(Access.WRONG_ORG_ADMIN).toString(), HttpMethod.GET)
            assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
        }

        @Test
        fun unauthorizedWhenMissingToken() {
            val response = apiAuthorizedRequest(path, port, null, null, HttpMethod.GET)
            assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
        }

    }

    @Nested
    internal inner class UpdateEditableFields {
        private val path = "/910244132/concepts/fields/editable"

        @Test
        fun ableToGetCatalogFieldsForAllOrgRoles() {
            val body = EditableFields("910244132", "123")
            val response = apiAuthorizedRequest(path, port, mapper.writeValueAsString(body), JwtToken(Access.ORG_ADMIN).toString(), HttpMethod.POST)

            assertEquals(HttpStatus.OK.value(), response["status"])

            val result: EditableFields = mapper.readValue(response["body"] as String)
            assertEquals(body, result)
        }

        @Test
        fun forbiddenForWrongOrgAndNonAdminRoles() {
            val body = EditableFields("910244132", "123")
            val readRole = apiAuthorizedRequest(path, port, mapper.writeValueAsString(body), JwtToken(Access.ORG_READ).toString(), HttpMethod.POST)
            val writeRole = apiAuthorizedRequest(path, port, mapper.writeValueAsString(body), JwtToken(Access.ORG_WRITE).toString(), HttpMethod.POST)
            val wrongOrg = apiAuthorizedRequest(path, port, mapper.writeValueAsString(body), JwtToken(Access.WRONG_ORG_ADMIN).toString(), HttpMethod.POST)

            assertEquals(HttpStatus.FORBIDDEN.value(), readRole["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), writeRole["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), wrongOrg["status"])
        }

        @Test
        fun unauthorizedWhenMissingToken() {
            val body = EditableFields("910244132", "123")
            val response = apiAuthorizedRequest(path, port, mapper.writeValueAsString(body), null, HttpMethod.POST)
            assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
        }

    }
}
