package no.digdir.catalog_admin_service.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.digdir.catalog_admin_service.model.*
import no.digdir.catalog_admin_service.utils.ApiTestContext
import no.digdir.catalog_admin_service.utils.FIELD_0
import no.digdir.catalog_admin_service.utils.apiAuthorizedRequest
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

            val expected = Fields(editable = EditableFields("910244132", null), internal = listOf(FIELD_0))
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
        fun ableToUpdateEditableFields() {
            val operations = listOf(JsonPatchOperation(op = OpEnum.ADD, "/domainCodeListId", "123"))
            val response = apiAuthorizedRequest(path, port, mapper.writeValueAsString(operations), JwtToken(Access.ORG_ADMIN).toString(), HttpMethod.PATCH)

            assertEquals(HttpStatus.OK.value(), response["status"])

            val result: EditableFields = mapper.readValue(response["body"] as String)
            val expected = EditableFields("910244132", "123")
            assertEquals(expected, result)
        }

        @Test
        fun badRequestWhenUpdatingCatalogId() {
            val operations = listOf(JsonPatchOperation(op = OpEnum.ADD, "/catalogId", "12345678"))
            val response = apiAuthorizedRequest(path, port, mapper.writeValueAsString(operations), JwtToken(Access.ORG_ADMIN).toString(), HttpMethod.PATCH)

            assertEquals(HttpStatus.BAD_REQUEST.value(), response["status"])
        }

        @Test
        fun forbiddenForWrongOrgAndNonAdminRoles() {
            val body = listOf(JsonPatchOperation(op = OpEnum.ADD, "/domainCodeListId", "123"))
            val readRole = apiAuthorizedRequest(path, port, mapper.writeValueAsString(body), JwtToken(Access.ORG_READ).toString(), HttpMethod.PATCH)
            val writeRole = apiAuthorizedRequest(path, port, mapper.writeValueAsString(body), JwtToken(Access.ORG_WRITE).toString(), HttpMethod.PATCH)
            val wrongOrg = apiAuthorizedRequest(path, port, mapper.writeValueAsString(body), JwtToken(Access.WRONG_ORG_ADMIN).toString(), HttpMethod.PATCH)

            assertEquals(HttpStatus.FORBIDDEN.value(), readRole["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), writeRole["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), wrongOrg["status"])
        }

        @Test
        fun unauthorizedWhenMissingToken() {
            val body = listOf(JsonPatchOperation(op = OpEnum.ADD, "/domainCodeListId", "123"))
            val response = apiAuthorizedRequest(path, port, mapper.writeValueAsString(body), null, HttpMethod.PATCH)
            assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
        }

    }

    @Nested
    internal inner class CreateInternalField {
        private val path = "/910244132/concepts/fields/internal"

        @Test
        fun ableToCreateInternalField() {
            val body = FieldToBeCreated(
                MultiLanguageTexts(nb = "label", nn = "label", en = "label"),
                MultiLanguageTexts(nb = "description", nn = "description", en = "description"),
                FieldType.BOOLEAN,
                null,
                null
            )
            val response = apiAuthorizedRequest(path, port, mapper.writeValueAsString(body), JwtToken(Access.ORG_ADMIN).toString(), HttpMethod.POST)

            assertEquals(HttpStatus.OK.value(), response["status"])

            val responseHeaders: HttpHeaders = response["header"] as HttpHeaders
            val location = responseHeaders.location
            assertNotNull(location)

            val getResponse = apiAuthorizedRequest(location.toString(), port, null, JwtToken(Access.ORG_READ).toString(), HttpMethod.GET)
            assertEquals(HttpStatus.OK.value(), getResponse["status"])
            val result: Field = mapper.readValue(getResponse["body"] as String)
            val expected = Field(
                id = result.id,
                catalogId = "910244132",
                label = MultiLanguageTexts(nb = "label", nn = "label", en = "label"),
                description = MultiLanguageTexts(nb = "description", nn = "description", en = "description"),
                type = FieldType.BOOLEAN,
                location = FieldLocation.MAIN_COLUMN,
                codeListId = null
            )
            assertEquals(expected, result)
        }

        @Test
        fun forbiddenForWrongOrgAndNonAdminRoles() {
            val body = FieldToBeCreated(MultiLanguageTexts(nb = "Test", nn = "Test", en = "Test"), null, null, null, null)
            val readRole = apiAuthorizedRequest(path, port, mapper.writeValueAsString(body), JwtToken(Access.ORG_READ).toString(), HttpMethod.POST)
            val writeRole = apiAuthorizedRequest(path, port, mapper.writeValueAsString(body), JwtToken(Access.ORG_WRITE).toString(), HttpMethod.POST)
            val wrongOrg = apiAuthorizedRequest(path, port, mapper.writeValueAsString(body), JwtToken(Access.WRONG_ORG_ADMIN).toString(), HttpMethod.POST)

            assertEquals(HttpStatus.FORBIDDEN.value(), readRole["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), writeRole["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), wrongOrg["status"])
        }

        @Test
        fun unauthorizedWhenMissingToken() {
            val body = FieldToBeCreated(MultiLanguageTexts(nb = "Test", nn = "Test", en = "Test"), null, null, null, null)
            val response = apiAuthorizedRequest(path, port, mapper.writeValueAsString(body), null, HttpMethod.POST)
            assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
        }

    }

    @Nested
    internal inner class GetInternalField {
        private val path = "/910244132/concepts/fields/internal/field-0"

        @Test
        fun ableToGetFieldForAllOrgRoles() {
            val readRsp = apiAuthorizedRequest(path, port, null, JwtToken(Access.ORG_READ).toString(), HttpMethod.GET)
            val writeRsp = apiAuthorizedRequest(path, port, null, JwtToken(Access.ORG_WRITE).toString(), HttpMethod.GET)
            val adminRsp = apiAuthorizedRequest(path, port, null, JwtToken(Access.ORG_ADMIN).toString(), HttpMethod.GET)

            assertEquals(HttpStatus.OK.value(), readRsp["status"])
            assertEquals(HttpStatus.OK.value(), writeRsp["status"])
            assertEquals(HttpStatus.OK.value(), adminRsp["status"])

            val readResult: Field = mapper.readValue(readRsp["body"] as String)
            val writeResult: Field = mapper.readValue(writeRsp["body"] as String)
            val adminResult: Field = mapper.readValue(adminRsp["body"] as String)

            assertEquals(FIELD_0, readResult)
            assertEquals(FIELD_0, writeResult)
            assertEquals(FIELD_0, adminResult)
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

        @Test
        fun notFoundForWrongId() {
            val response = apiAuthorizedRequest("/910244132/concepts/fields/internal/invalid", port, null, JwtToken(Access.ORG_ADMIN).toString(), HttpMethod.GET)
            assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
        }
    }

    @Nested
    internal inner class DeleteInternalField {
        private val path = "/910244132/concepts/fields/internal/field-0"

        @Test
        fun adminIsAbleToDeleteField() {
            val deleteResponse = apiAuthorizedRequest(path, port, null, JwtToken(Access.ORG_ADMIN).toString(), HttpMethod.DELETE)
            assertEquals(HttpStatus.NO_CONTENT.value(), deleteResponse["status"])

            val getResponse = apiAuthorizedRequest(path, port, null, JwtToken(Access.ORG_ADMIN).toString(), HttpMethod.GET)
            assertEquals(HttpStatus.NOT_FOUND.value(), getResponse["status"])
        }

        @Test
        fun forbiddenForWrongOrgAndNonAdminRoles() {
            val wrongOrg = apiAuthorizedRequest(path, port, null, JwtToken(Access.WRONG_ORG_ADMIN).toString(), HttpMethod.DELETE)
            val readRole = apiAuthorizedRequest(path, port, null, JwtToken(Access.ORG_READ).toString(), HttpMethod.DELETE)
            val writeRole = apiAuthorizedRequest(path, port, null, JwtToken(Access.ORG_WRITE).toString(), HttpMethod.DELETE)

            assertEquals(HttpStatus.FORBIDDEN.value(), wrongOrg["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), readRole["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), writeRole["status"])
        }

        @Test
        fun unauthorizedWhenMissingToken() {
            val response = apiAuthorizedRequest(path, port, null, null, HttpMethod.DELETE)
            assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
        }

        @Test
        fun notFoundForWrongId() {
            val response = apiAuthorizedRequest("/910244132/concepts/fields/internal/invalid", port, null, JwtToken(Access.ORG_ADMIN).toString(), HttpMethod.DELETE)
            assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
        }
    }

    @Nested
    internal inner class UpdateInternalField {
        private val path = "/910244132/concepts/fields/internal/field-0"

        @Test
        fun adminIsAbleToUpdateField() {
            val operations = listOf(JsonPatchOperation(op = OpEnum.REPLACE, "/description/nn", "New description"))
            val response = apiAuthorizedRequest(path, port, mapper.writeValueAsString(operations), JwtToken(Access.ORG_ADMIN).toString(), HttpMethod.PATCH)
            assertEquals(HttpStatus.OK.value(), response["status"])

            val result: Field = mapper.readValue(response["body"] as String)
            assertEquals(FIELD_0.copy(description = FIELD_0.description.copy(nn = "New description")), result)
        }

        @Test
        fun forbiddenForWrongOrgAndNonAdminRoles() {
            val operations = listOf(JsonPatchOperation(op = OpEnum.REPLACE, "/description/nn", "New description"))
            val wrongOrg = apiAuthorizedRequest(path, port, mapper.writeValueAsString(operations), JwtToken(Access.WRONG_ORG_ADMIN).toString(), HttpMethod.PATCH)
            val readRole = apiAuthorizedRequest(path, port, mapper.writeValueAsString(operations), JwtToken(Access.ORG_READ).toString(), HttpMethod.PATCH)
            val writeRole = apiAuthorizedRequest(path, port, mapper.writeValueAsString(operations), JwtToken(Access.ORG_WRITE).toString(), HttpMethod.PATCH)

            assertEquals(HttpStatus.FORBIDDEN.value(), wrongOrg["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), readRole["status"])
            assertEquals(HttpStatus.FORBIDDEN.value(), writeRole["status"])
        }

        @Test
        fun unauthorizedWhenMissingToken() {
            val operations = listOf(JsonPatchOperation(op = OpEnum.REPLACE, "/description/nn", "New description"))
            val response = apiAuthorizedRequest(path, port, mapper.writeValueAsString(operations), null, HttpMethod.DELETE)
            assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
        }

        @Test
        fun notFoundForWrongId() {
            val operations = listOf(JsonPatchOperation(op = OpEnum.REPLACE, "/description/nn", "New description"))
            val response = apiAuthorizedRequest("/910244132/concepts/fields/internal/invalid", port, mapper.writeValueAsString(operations), JwtToken(Access.ORG_ADMIN).toString(), HttpMethod.DELETE)
            assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
        }
    }
}
