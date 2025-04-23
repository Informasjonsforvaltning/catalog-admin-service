package no.digdir.catalog_admin_service.controller

import no.digdir.catalog_admin_service.model.CodeList
import no.digdir.catalog_admin_service.service.CodeListService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class ConceptSubjectsController(private val codeListService: CodeListService) {

    @GetMapping(value = ["/concept-subjects"], produces = ["text/turtle"])
    fun getAllConceptSubjectCodeListsRDF(): ResponseEntity<String> =
        ResponseEntity(codeListService.getAllConceptSubjectCodeListsRDF(), HttpStatus.OK)

    @GetMapping(value = ["/{catalogId}/concepts/code-list/subjects"], produces = ["text/turtle"])
    fun getConceptSubjectsForCatalogRDF(@PathVariable catalogId: String): ResponseEntity<String> =
        codeListService.getConceptSubjectsForCatalogRDF(catalogId)
            ?.let { ResponseEntity(it, HttpStatus.OK) }
            ?: ResponseEntity(HttpStatus.NOT_FOUND)

    @GetMapping(value = ["/concept-subjects"], produces = ["application/json"])
    fun getAllConceptSubjectCodeLists(): ResponseEntity<List<CodeList>> =
        ResponseEntity(codeListService.getAllConceptSubjectCodeLists(), HttpStatus.OK)

    @GetMapping(value = ["/{catalogId}/concepts/code-list/subjects"], produces = ["application/json"])
    fun getConceptSubjectsForCatalog(@PathVariable catalogId: String): ResponseEntity<CodeList> =
        codeListService.getConceptSubjectsForCatalog(catalogId)
            ?.let { ResponseEntity(it, HttpStatus.OK) }
            ?: ResponseEntity(HttpStatus.NOT_FOUND)

}
