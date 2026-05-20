package no.digdir.catalog_admin_service.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.digdir.catalog_admin_service.utils.ApiTestContext.Companion.postgresContainer
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.flywaydb.core.Flyway
import org.postgresql.util.PGobject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.StandardCharsets
import java.sql.DriverManager


fun apiGet(port: Int, endpoint: String, acceptHeader: String?): Map<String, Any> {

    return try {
        val connection = URL("http://localhost:$port$endpoint").openConnection() as HttpURLConnection
        if (acceptHeader != null) connection.setRequestProperty("Accept", acceptHeader)
        connection.connect()

        if (isOK(connection.responseCode)) {
            val responseBody = connection.inputStream.bufferedReader().use(BufferedReader::readText)
            mapOf(
                "body" to responseBody,
                "header" to connection.headerFields.toString(),
                "status" to connection.responseCode
            )
        } else {
            mapOf(
                "status" to connection.responseCode,
                "header" to " ",
                "body" to " "
            )
        }
    } catch (e: Exception) {
        mapOf(
            "status" to e.toString(),
            "header" to " ",
            "body" to " "
        )
    }
}

fun apiAuthorizedRequest(
    path: String, port: Int, body: String?, token: String?, httpMethod: HttpMethod,
    accept: MediaType = MediaType.APPLICATION_JSON
): Map<String, Any> {

    val request = RestTemplate()
    request.requestFactory = HttpComponentsClientHttpRequestFactory()
    val url = "http://localhost:$port$path"
    val headers = HttpHeaders()
    headers.accept = listOf(accept)
    token?.let { headers.setBearerAuth(it) }
    headers.contentType = MediaType.APPLICATION_JSON
    val entity: HttpEntity<String> = HttpEntity(body, headers)

    return try {
        val response = request.exchange(url, httpMethod, entity, String::class.java)
        mapOf(
            "body" to (response.body ?: ""),
            "header" to response.headers,
            "status" to response.statusCode.value()
        )

    } catch (e: HttpClientErrorException) {
        mapOf(
            "status" to e.statusCode.value(),
            "header" to " ",
            "body" to e.toString()
        )
    } catch (e: Exception) {
        mapOf(
            "status" to e.toString(),
            "header" to " ",
            "body" to " "
        )
    }
}

fun apiAuthorizedMultipartLogo(
    path: String, port: Int, filePath: String, token: String?
): Map<String, Any> {
    val file = ClassPathResource(filePath)
    val parts: MultiValueMap<String, Any> = LinkedMultiValueMap()
    parts.add("logo", file)
    val request = RestTemplate()
    request.requestFactory = HttpComponentsClientHttpRequestFactory()
    val url = "http://localhost:$port$path"
    val headers = HttpHeaders()
    headers.contentType = MediaType.MULTIPART_FORM_DATA
    token?.let { headers.setBearerAuth(it) }
    val entity: HttpEntity<MultiValueMap<String, Any>> = HttpEntity(parts, headers)

    return try {
        val response = request.exchange(url, HttpMethod.POST, entity, String::class.java)
        mapOf(
            "body" to (response.body ?: ""),
            "header" to response.headers,
            "status" to response.statusCode.value()
        )

    } catch (e: HttpClientErrorException) {
        mapOf(
            "status" to e.statusCode.value(),
            "header" to " ",
            "body" to e.toString()
        )
    } catch (e: Exception) {
        mapOf(
            "status" to e.toString(),
            "header" to " ",
            "body" to " "
        )
    }
}


private fun isOK(response: Int?): Boolean =
    if (response == null) false
    else HttpStatus.resolve(response)?.is2xxSuccessful == true

private val mapper = jacksonObjectMapper()

private fun toJsonb(value: Any?): PGobject {
    val pg = PGobject()
    pg.type = "jsonb"
    pg.value = if (value != null) mapper.writeValueAsString(value) else null
    return pg
}

fun resetDB() {
    Flyway.configure()
        .dataSource(postgresContainer.getJdbcUrl(), DB_USER, DB_PASSWORD)
        .cleanDisabled(false)
        .load()
        .also { it.clean(); it.migrate() }

    DriverManager.getConnection(postgresContainer.getJdbcUrl(), DB_USER, DB_PASSWORD).use { conn ->

        conn.prepareStatement(
            "INSERT INTO catalog_users (id, catalog_id, name, email, telephone_number) VALUES (?, ?, ?, ?, ?)"
        ).use { stmt ->
            for (u in listOf(USER)) {
                stmt.setString(1, u.id)
                stmt.setString(2, u.catalogId)
                stmt.setString(3, u.name)
                stmt.setString(4, u.email)
                stmt.setString(5, u.telephoneNumber)
                stmt.addBatch()
            }
            stmt.executeBatch()
        }

        conn.prepareStatement(
            "INSERT INTO editable_fields (catalog_id, domain_code_list_id) VALUES (?, ?)"
        ).use { stmt ->
            for (ef in listOf(
                CODE_LIST_0.catalogId to CODE_LIST_0.id,
                CODE_LIST_1.catalogId to CODE_LIST_1.id
            )) {
                stmt.setString(1, ef.first)
                stmt.setString(2, ef.second)
                stmt.addBatch()
            }
            stmt.executeBatch()
        }

        conn.prepareStatement(
            "INSERT INTO internal_fields (id, catalog_id, label, description, type, location, code_list_id, enable_filter) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
        ).use { stmt ->
            for (f in listOf(FIELD_0)) {
                stmt.setString(1, f.id)
                stmt.setString(2, f.catalogId)
                stmt.setObject(3, toJsonb(f.label))
                stmt.setObject(4, toJsonb(f.description))
                stmt.setString(5, f.type.name)
                stmt.setString(6, f.location.name)
                stmt.setString(7, f.codeListId)
                val filter = f.enableFilter
                if (filter != null) stmt.setBoolean(8, filter) else stmt.setNull(8, java.sql.Types.BOOLEAN)
                stmt.addBatch()
            }
            stmt.executeBatch()
        }

        conn.prepareStatement(
            "INSERT INTO catalog_designs (catalog_id, background_color, font_color, logo_description, has_logo) VALUES (?, ?, ?, ?, ?)"
        ).use { stmt ->
            for (d in listOf(DESIGN_DBO)) {
                stmt.setString(1, d.catalogId)
                stmt.setString(2, d.backgroundColor)
                stmt.setString(3, d.fontColor)
                stmt.setString(4, d.logoDescription)
                stmt.setBoolean(5, d.hasLogo)
                stmt.addBatch()
            }
            stmt.executeBatch()
        }

        conn.prepareStatement(
            "INSERT INTO catalog_logos (catalog_id, content_type, base64_logo, filename) VALUES (?, ?, ?, ?)"
        ).use { stmt ->
            for (l in listOf(LOGO)) {
                stmt.setString(1, l.catalogId)
                stmt.setString(2, l.contentType)
                stmt.setString(3, l.base64Logo)
                stmt.setString(4, l.filename)
                stmt.addBatch()
            }
            stmt.executeBatch()
        }

        conn.prepareStatement(
            "INSERT INTO code_lists (id, name, catalog_id, description, codes) VALUES (?, ?, ?, ?, ?)"
        ).use { stmt ->
            for (cl in listOf(CODE_LIST_0, CODE_LIST_1, CODE_LIST_2, CODE_LIST_3)) {
                stmt.setString(1, cl.id)
                stmt.setString(2, cl.name)
                stmt.setString(3, cl.catalogId)
                stmt.setString(4, cl.description)
                stmt.setObject(5, toJsonb(cl.codes))
                stmt.addBatch()
            }
            stmt.executeBatch()
        }
    }
}

class TestResponseReader {
    private fun resourceAsReader(resourceName: String): Reader {
        return InputStreamReader(javaClass.classLoader.getResourceAsStream(resourceName)!!, StandardCharsets.UTF_8)
    }

    fun parseTurtleFile(filename: String): Model {
        val expected = ModelFactory.createDefaultModel()
        expected.read(resourceAsReader(filename), "", "TURTLE")
        return expected
    }
}
