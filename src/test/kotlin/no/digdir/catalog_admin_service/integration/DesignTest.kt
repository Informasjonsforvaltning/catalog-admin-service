package no.digdir.catalog_admin_service.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.digdir.catalog_admin_service.model.DesignDTO
import no.digdir.catalog_admin_service.utils.ApiTestContext
import no.digdir.catalog_admin_service.utils.DESIGN_DTO
import no.digdir.catalog_admin_service.utils.UPDATED_DESIGN_DTO
import no.digdir.catalog_admin_service.utils.apiAuthorizedMultipartLogo
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


        @Test
        fun updateDesign() {
            val response = apiAuthorizedRequest(
                path,
                port,
                mapper.writeValueAsString(UPDATED_DESIGN_DTO),
                JwtToken(Access.ORG_ADMIN).toString(),
                HttpMethod.POST
            )

            assertEquals(HttpStatus.OK.value(), response["status"])

            val result: DesignDTO = mapper.readValue(response["body"] as String)
            assertEquals(
                DESIGN_DTO.copy(
                    logoDescription = "New FDK Logo"
                ), result
            )
        }

        @Test
        fun findDesignUnauthorizedWhenMissingJwt() {
            val response = apiGet(port, "/910244132/design", null)
            assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
        }

        @Test
        fun designNotFoundInDB() {
            val response = apiAuthorizedRequest(
                "/123456789/design",
                port,
                null,
                JwtToken(Access.ROOT).toString(),
                HttpMethod.GET
            )
            assertEquals(HttpStatus.OK.value(), response["status"])
            val result: DesignDTO = mapper.readValue(response["body"] as String)
            assertEquals(result, DesignDTO(null, null, null))
        }

        @Test
        fun updateDesignForbiddenForOrgRead() {
            val response = apiAuthorizedRequest(
                path,
                port,
                mapper.writeValueAsString(UPDATED_DESIGN_DTO),
                JwtToken(Access.ORG_READ).toString(),
                HttpMethod.POST
            )
            assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
        }
    }
    @Nested
    internal inner class Logo {
        private val path = "/910244132/design/logo"

        @Test
        fun postUnauthorizedWhenMissingToken() {
            val response = apiAuthorizedMultipartLogo(path, port, "safe.png", null)
            assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
        }

        @Test
        fun postForbiddenForReadAccess() {
            val response = apiAuthorizedMultipartLogo(path, port, "safe.png", JwtToken(Access.ORG_READ).toString())
            assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
        }

        @Test
        fun postForbiddenForWriteAccess() {
            val response = apiAuthorizedMultipartLogo(path, port, "safe.png", JwtToken(Access.ORG_WRITE).toString())
            assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
        }

        @Test
        fun uploadPNG() {
            val response = apiAuthorizedMultipartLogo(path, port, "safe.png", JwtToken(Access.ORG_ADMIN).toString())
            assertEquals(HttpStatus.OK.value(), response["status"])
        }

        @Test
        fun badRequestWhenUploadingScaryPNG() {
            val response = apiAuthorizedMultipartLogo(path, port, "scary.png", JwtToken(Access.ORG_ADMIN).toString())
            assertEquals(HttpStatus.BAD_REQUEST.value(), response["status"])
        }

        @Test
        fun uploadSVG() {
            val response = apiAuthorizedMultipartLogo(path, port, "safe.svg", JwtToken(Access.ORG_ADMIN).toString())
            assertEquals(HttpStatus.OK.value(), response["status"])
        }

        @Test
        fun badRequestWhenUploadingScarySVG() {
            val response = apiAuthorizedMultipartLogo(path, port, "scary.svg", JwtToken(Access.ORG_ADMIN).toString())
            assertEquals(HttpStatus.BAD_REQUEST.value(), response["status"])
        }

        @Test
        fun getLogo() {
            val response = apiAuthorizedRequest(path, port, null, JwtToken(Access.ORG_READ).toString(), HttpMethod.GET)
            assertEquals(HttpStatus.OK.value(), response["status"])
        }

        @Test
        fun getLogoUnauthorizedWhenMissingToken() {
            val response = apiAuthorizedRequest(path, port, null, null, HttpMethod.GET)
            assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
        }

        @Test
        fun deleteLogo() {
            val deleteResponse = apiAuthorizedRequest(path, port, null, JwtToken(Access.ORG_ADMIN).toString(), HttpMethod.DELETE)
            assertEquals(HttpStatus.NO_CONTENT.value(), deleteResponse["status"])

            val getResponse = apiAuthorizedRequest(path, port, null, JwtToken(Access.ORG_READ).toString(), HttpMethod.GET)
            assertEquals(HttpStatus.NOT_FOUND.value(), getResponse["status"])
        }

        @Test
        fun deleteLogoUnauthorizedWhenMissingToken() {
            val response = apiAuthorizedRequest(path, port, null, null, HttpMethod.DELETE)
            assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
        }

        @Test
        fun deleteLogoForbiddenForReadAccess() {
            val response = apiAuthorizedRequest(path, port, null, JwtToken(Access.ORG_READ).toString(), HttpMethod.DELETE)
            assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
        }

    }
}