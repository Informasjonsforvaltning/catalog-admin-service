package no.digdir.catalog_admin_service.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.util.*
import no.digdir.catalog_admin_service.model.CodeList
import no.digdir.catalog_admin_service.model.CodeListToBeCreated
import no.digdir.catalog_admin_service.model.CodeLists
import no.digdir.catalog_admin_service.repository.CodeListRepository
import org.springframework.stereotype.Service
import jakarta.json.Json
import jakarta.json.JsonException
import java.io.StringReader
import no.digdir.catalog_admin_service.model.JsonPatchOperation
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

private val mapper = jacksonObjectMapper()
private val logger = LoggerFactory.getLogger(CodeListService::class.java)

@Service
class CodeListService(private val codeListRepository: CodeListRepository) {
    fun getCodeLists(catalogId: String): CodeLists =
        CodeLists(codeLists = codeListRepository.findCodeListsByCatalogId(catalogId))

    fun getCodeListById(catalogId: String, codeListId: String): CodeList? =
        codeListRepository.findCodeListByIdAndCatalogId(codeListId, catalogId)

    fun deleteCodeListById(codeListId: String) =
        codeListRepository.deleteById(codeListId)

    fun createCodeList(data: CodeListToBeCreated, catalogId: String): CodeList =
        CodeList(
            id = UUID.randomUUID().toString(),
            name = data.name,
            catalogId = catalogId,
            description = data.description,
            codes = data.codes
        ).let { codeListRepository.insert(it) }

    private fun patchCodeList(codeList: CodeList, operations: List<JsonPatchOperation>): CodeList {
        if (operations.isNotEmpty()) {
            with(mapper) {
                val changes = Json.createReader(StringReader(writeValueAsString(operations))).readArray()
                val original = Json.createReader(StringReader(writeValueAsString(codeList))).readObject()

                return Json.createPatch(changes).apply(original)
                    .let { readValue(it.toString()) }
            }
        }
        return codeList
    }

    fun updateCodeList(codeListId: String, catalogId: String, operations: List<JsonPatchOperation>): CodeList? {
        val patched = codeListRepository.findCodeListByIdAndCatalogId(codeListId, catalogId)
            ?.let { dbCodeList ->
                try {
                    patchCodeList(dbCodeList, operations)
                } catch (ex: Exception) {
                    logger.error("PATCH failed for $codeListId", ex)
                    when (ex) {
                        is JsonException -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, ex.message)
                        is JsonProcessingException -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, ex.message)
                        is IllegalArgumentException -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, ex.message)
                        else -> throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.message)
                    }
                }
            }

        when {
            patched != null && patched.id != codeListId -> throw ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Unable to patch ID"
            )

            patched != null && patched.catalogId != catalogId -> throw ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Unable to patch catalogID"
            )
        }

        return patched?.let { codeListRepository.save(it) }
    }
}

