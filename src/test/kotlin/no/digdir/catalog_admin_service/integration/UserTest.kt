package no.digdir.catalog_admin_service.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.digdir.catalog_admin_service.model.JsonPatchOperation
import no.digdir.catalog_admin_service.model.OpEnum
import no.digdir.catalog_admin_service.model.User
import no.digdir.catalog_admin_service.model.Users
import no.digdir.catalog_admin_service.utils.ApiTestContext
import no.digdir.catalog_admin_service.utils.USER
import no.digdir.catalog_admin_service.utils.USER_TO_BE_CREATED
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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody
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
class UserTest : ApiTestContext() {
    val path = "/910244132/general/user-list"

    @Test
    fun findUsers() {
        val response = apiAuthorizedRequest(
            path,
            port,
            null,
            JwtToken(Access.ORG_WRITE).toString(),
            HttpMethod.GET
        )
        assertEquals(HttpStatus.OK.value(), response["status"])
        val result: Users = mapper.readValue(response["body"] as String)
        val expected = Users(users = listOf(USER))
        assertEquals(expected, result)
    }

    @Test
    fun findUserById() {
        val response = apiAuthorizedRequest(
            "$path/123",
            port,
            null,
            JwtToken(Access.ORG_READ).toString(),
            HttpMethod.GET
        )
        assertEquals(HttpStatus.OK.value(), response["status"])
        val result: User = mapper.readValue(response["body"] as String)
        assertEquals(USER, result)
    }

    @Test
    fun findUsersUnauthorizedWhenMissingJwt() {
        val response = apiGet(port, path, null)
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
    }

    @Test
    fun findUserByIdUnauthorizedWhenMissingJwt() {
        val response = apiGet(port, "$path/123", null)
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
    }

    @Test
    fun userNotFound() {
        val response = apiAuthorizedRequest(
            "$path/xxx",
            port,
            null,
            JwtToken(Access.ROOT).toString(),
            HttpMethod.GET
        )
        assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
    }

    @Test
    fun findUsersForbiddenForWrongOrg() {
        val response = apiAuthorizedRequest(
            "/910244132/general/user-list",
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
            "$path/123",
            port,
            null,
            JwtToken(Access.WRONG_ORG_ADMIN).toString(),
            HttpMethod.GET
        )
        assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
    }

    @Test
    fun findUserByIdNotFoundForUserNotInCatalog() {
        val response = apiAuthorizedRequest(
            "/123456789/general/user-list/123",
            port,
            null,
            JwtToken(Access.WRONG_ORG_ADMIN).toString(),
            HttpMethod.GET
        )
        assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
    }

    @Test
    fun deleteCodeList() {

        val preResponse = apiAuthorizedRequest(
            "$path/123",
            port,
            null,
            JwtToken(Access.ORG_ADMIN).toString(),
            HttpMethod.GET
        )
        assertEquals(HttpStatus.OK.value(), preResponse["status"])

        val deleteResponse =
            apiAuthorizedRequest("$path/123", port, null, JwtToken(Access.ORG_ADMIN).toString(), HttpMethod.DELETE)
        assertEquals(HttpStatus.NO_CONTENT.value(), deleteResponse["status"])

        val postResponse = apiAuthorizedRequest(
            "$path/123",
            port,
            null,
            JwtToken(Access.ORG_ADMIN).toString(),
            HttpMethod.DELETE
        )
        assertEquals(HttpStatus.NOT_FOUND.value(), postResponse["status"])
    }

    @Test
    fun deleteUserWrongOrg() {
        val path = "/123456789/general/user-list/123"
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
    fun deleteUserReadOnly() {
        val path = "$path/123"
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
    fun deleteUserThatDoesNotExist() {
        val path = "$path/xxx"
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
    fun createUser() {
        val response = apiAuthorizedRequest(
            path,
            port,
            mapper.writeValueAsString(USER_TO_BE_CREATED),
            JwtToken(Access.ORG_ADMIN).toString(),
            HttpMethod.POST
        )
        val responseHeaders: HttpHeaders = response["header"] as HttpHeaders
        val location = responseHeaders.location
        assertNotNull(location)

        val getResponse =
            apiAuthorizedRequest(location.toString(), port, null, JwtToken(Access.ORG_READ).toString(), HttpMethod.GET)
        assertEquals(HttpStatus.OK.value(), getResponse["status"])
        val result: User = mapper.readValue(getResponse["body"] as String)
        val expected = User(
            name = USER_TO_BE_CREATED.name,
            telephoneNumber = USER_TO_BE_CREATED.telephoneNumber,
            userId = result.userId,
            catalogId = "910244132",
            email = USER_TO_BE_CREATED.email
        )
        assertEquals(expected, result)
    }


@Nested
internal inner class Update {

    @Test
    fun updateUserUnauthorizedWhenMissingJwt() {
        val response = apiGet(port, path, null)
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
    }

    @Test
    fun updateUserForbiddenForReadOnly() {
        val operations = listOf(JsonPatchOperation(op = OpEnum.REPLACE, "/name", "Updated name"))
        val response = apiAuthorizedRequest(
            "$path/123",
            port,
            mapper.writeValueAsString(operations),
            JwtToken(Access.ORG_READ).toString(),
            HttpMethod.PATCH
        )
        assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
    }

    @Test
    fun updateUserForbiddenForWrongOrg() {
        val operations = listOf(JsonPatchOperation(op = OpEnum.REPLACE, "/name", "Updated name"))
        val response = apiAuthorizedRequest(
            "$path/123",
            port,
            mapper.writeValueAsString(operations),
            JwtToken(Access.WRONG_ORG_ADMIN).toString(),
            HttpMethod.PATCH
        )
        assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
    }

    @Test
    fun updateUser() {
        val operations = listOf(JsonPatchOperation(op = OpEnum.REPLACE, "/name", "Updated name"))
        val response = apiAuthorizedRequest(
            "$path/123",
            port,
            mapper.writeValueAsString(operations),
            JwtToken(Access.ORG_ADMIN).toString(),
            HttpMethod.PATCH
        )

        assertEquals(HttpStatus.OK.value(), response["status"])

        val result: User = mapper.readValue(response["body"] as String)
        assertEquals(
            USER.copy(
                name = "Updated name"
            ), result
        )
    }

    @Test
    fun addNewEmailToUser() {
        val operations = listOf(JsonPatchOperation(op = OpEnum.ADD, "/email", "new@mail.com"))
        val response = apiAuthorizedRequest(
            "$path/123",
            port,
            mapper.writeValueAsString(operations),
            JwtToken(Access.ORG_ADMIN).toString(),
            HttpMethod.PATCH
        )
        assertEquals(HttpStatus.OK.value(), response["status"])

        val result: User = mapper.readValue(response["body"] as String)
        assertEquals(
            USER.copy(
                email = "new@mail.com"
            ), result
        )

    }

    @Test
    fun updateUserNotFound() {
        val operations = listOf(JsonPatchOperation(op = OpEnum.REPLACE, "/email", "mail@mail.com"))
        val response = apiAuthorizedRequest(
            "$path/xxx",
            port,
            mapper.writeValueAsString(operations),
            JwtToken(Access.ORG_ADMIN).toString(),
            HttpMethod.PATCH
        )
        assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
    }

    @Test
    fun updateUserWithCopy() {
        val operations = listOf(JsonPatchOperation(op = OpEnum.COPY, path = "/name", from = "/email"))
        val response = apiAuthorizedRequest(
            "$path/123",
            port,
            mapper.writeValueAsString(operations),
            JwtToken(Access.ORG_ADMIN).toString(),
            HttpMethod.PATCH
        )
        assertEquals(HttpStatus.OK.value(), response["status"])

        val result: User = mapper.readValue(response["body"] as String)
        assertEquals(result.name, result.name)
        assertEquals(
            USER.copy(
                name = "test@mail.com"
            ), result
        )
    }

    @Test
    fun updateUserRemove() {
        val operations = listOf(JsonPatchOperation(op = OpEnum.REMOVE, path = "/telephoneNumber"))
        val response = apiAuthorizedRequest(
            "$path/123",
            port,
            mapper.writeValueAsString(operations),
            JwtToken(Access.ORG_ADMIN).toString(),
            HttpMethod.PATCH
        )
        assertEquals(HttpStatus.OK.value(), response["status"])

        val result: User = mapper.readValue(response["body"] as String)
        assertEquals(
            USER.copy(
                telephoneNumber = null
            ), result
        )
    }

    @Test
    fun cannotRemoveRequiredValue() {
        val operations = listOf(JsonPatchOperation(op = OpEnum.REMOVE, path = "name"))
        val response = apiAuthorizedRequest(
            "$path/123",
            port,
            mapper.writeValueAsString(operations),
            JwtToken(Access.ORG_ADMIN).toString(),
            HttpMethod.PATCH
        )
        assertEquals(HttpStatus.BAD_REQUEST.value(), response["status"])
    }

    @Test
    fun updateUserMove() {
        val operations = listOf(JsonPatchOperation(op = OpEnum.MOVE, path = "/name", from = "/email"))
        val response = apiAuthorizedRequest(
            "$path/123",
            port,
            mapper.writeValueAsString(operations),
            JwtToken(Access.ORG_ADMIN).toString(),
            HttpMethod.PATCH
        )

        assertEquals(HttpStatus.OK.value(), response["status"])

        val result: User = mapper.readValue(response["body"] as String)
        assertEquals(
            USER.copy(
                name = "test@mail.com",
                email = null,
            ), result
        )
    }

    @Test
    fun badRequestWhenUpdatingId() {
        val operations = listOf(JsonPatchOperation(op = OpEnum.REPLACE, path = "/userId", value = "1111"))
        val response = apiAuthorizedRequest(
            "$path/123",
            port,
            mapper.writeValueAsString(operations),
            JwtToken(Access.ORG_ADMIN).toString(),
            HttpMethod.PATCH
        )

        assertEquals(HttpStatus.BAD_REQUEST.value(), response["status"])
    }
}
}
