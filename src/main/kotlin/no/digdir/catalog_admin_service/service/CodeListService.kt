package no.digdir.catalog_admin_service.service

import no.digdir.catalog_admin_service.model.CodeList
import no.digdir.catalog_admin_service.model.CodeLists
import no.digdir.catalog_admin_service.repository.CodeListRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class CodeListService(private val codeListRepository: CodeListRepository) {
    fun getCodeLists(): CodeLists =
        CodeLists(codeLists = codeListRepository.findAll())

    fun getCodeListById(codeListId: String): CodeList? =
        codeListRepository.findByIdOrNull(codeListId)
}