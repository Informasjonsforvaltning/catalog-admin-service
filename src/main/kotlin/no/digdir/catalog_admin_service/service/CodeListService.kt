package no.digdir.catalog_admin_service.service

import java.util.*
import no.digdir.catalog_admin_service.model.CodeList
import no.digdir.catalog_admin_service.model.CodeListToBeCreated
import no.digdir.catalog_admin_service.model.CodeLists
import no.digdir.catalog_admin_service.repository.CodeListRepository
import org.springframework.stereotype.Service
import no.digdir.catalog_admin_service.model.JsonPatchOperation
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(CodeListService::class.java)

@Service
class CodeListService(private val codeListRepository: CodeListRepository) {
    fun getCodeLists(catalogId: String): CodeLists =
        CodeLists(codeLists = codeListRepository.findCodeListsByCatalogId(catalogId))

    fun getCodeListById(catalogId: String, codeListId: String): CodeList? =
        codeListRepository.findCodeListByIdAndCatalogId(codeListId, catalogId)

    fun deleteCodeListById(codeListId: String) =
        try {
            codeListRepository.deleteById(codeListId)
        } catch (ex: Exception) {
            logger.error("Failed to delete code-list with id $codeListId", ex)
            throw ex
        }

    fun createCodeList(data: CodeListToBeCreated, catalogId: String): CodeList =
        try {
            CodeList(
                id = UUID.randomUUID().toString(),
                name = data.name,
                catalogId = catalogId,
                description = data.description,
                codes = data.codes
            ).let { codeListRepository.insert(it) }
        } catch (ex: Exception) {
            logger.error("Failed to create code-list for catalog $catalogId", ex)
            throw ex
        }

    fun updateCodeList(codeListId: String, catalogId: String, operations: List<JsonPatchOperation>): CodeList? =
        try {
            codeListRepository.findCodeListByIdAndCatalogId(codeListId, catalogId)
                ?.let { dbCodeList -> patchOriginal(dbCodeList, operations) }
                ?.let { codeListRepository.save(it) }
        } catch (ex: Exception) {
            logger.error("Failed to update code-list with id $codeListId in catalog $catalogId", ex)
            throw ex
        }
}
