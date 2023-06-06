package no.digdir.catalog_admin_service.service

import no.digdir.catalog_admin_service.model.CodeList
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
}
