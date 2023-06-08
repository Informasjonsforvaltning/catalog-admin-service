package no.digdir.catalog_admin_service.service

import java.util.*
import no.digdir.catalog_admin_service.model.CodeList
import no.digdir.catalog_admin_service.model.CodeListToBeCreated
import no.digdir.catalog_admin_service.model.CodeLists
import no.digdir.catalog_admin_service.repository.CodeListRepository
import org.springframework.stereotype.Service

@Service
class CodeListService(private val codeListRepository: CodeListRepository) {
    fun getCodeLists(catalogId: String): CodeLists =
        CodeLists(codeLists = codeListRepository.findCodeListsByCatalogId(catalogId))

    fun getCodeListById(catalogId: String, codeListId: String): CodeList? =
        codeListRepository.findCodeListByIdAndCatalogId(codeListId, catalogId)

    fun deleteCodeListById(codeListId: String) =
        codeListRepository.deleteById(codeListId)

    fun createCodeList(data: CodeListToBeCreated): CodeList =
        CodeList(
            id = UUID.randomUUID().toString(),
            name = data.name,
            catalogId = data.catalogId,
            description = data.description,
            codes = data.codes
        ).let { codeListRepository.insert(it) }
}
