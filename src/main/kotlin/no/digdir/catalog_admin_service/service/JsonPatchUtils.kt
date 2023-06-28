package no.digdir.catalog_admin_service.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.json.Json
import jakarta.json.JsonException
import no.digdir.catalog_admin_service.model.JsonPatchOperation
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.io.StringReader

inline fun <reified T> patchOriginal(original: T, operations: List<JsonPatchOperation>): T {
    when {
        operations.find { patch -> patch.path == "/id" } != null -> throw ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Unable to patch ID"
        )

        operations.find { patch -> patch.path == "/catalogId" } != null -> throw ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Unable to patch catalogID"
        )

        operations.find { patch -> patch.path == "/userId" } != null -> throw ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Unable to patch userId"
        )
    }

    try {
        return applyPatch(original, operations)
    } catch (ex: Exception) {
        when (ex) {
            is JsonException -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, ex.message)
            is JsonProcessingException -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, ex.message)
            is IllegalArgumentException -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, ex.message)
            else -> throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.message)
        }
    }
}

inline fun <reified T> applyPatch(originalObject: T, operations: List<JsonPatchOperation>): T {
    if (operations.isNotEmpty()) {
        with(jacksonObjectMapper()) {
            val changes = Json.createReader(StringReader(writeValueAsString(operations))).readArray()
            val original = Json.createReader(StringReader(writeValueAsString(originalObject))).readObject()

            return Json.createPatch(changes).apply(original)
                .let { readValue(it.toString()) }
        }
    }
    return originalObject
}
