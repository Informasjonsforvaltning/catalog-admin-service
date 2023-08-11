package no.digdir.catalog_admin_service.controller

import no.digdir.catalog_admin_service.service.CodeListService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@CrossOrigin
@RequestMapping(produces = ["text/turtle"])
class ConceptSubjectsController(private val codeListService: CodeListService) {

    @GetMapping(value = ["/concept-subjects"])
    fun getAllConceptSubjectCodeLists(): ResponseEntity<String> =
        ResponseEntity(codeListService.getAllConceptSubjectCodeLists(), HttpStatus.OK)

    @GetMapping(value = ["/{catalogId}/concepts/code-list/subjects"])
    fun getConceptSubjectsForCatalog(@PathVariable catalogId: String): ResponseEntity<String> =
        codeListService.getConceptSubjectsForCatalog(catalogId)
            ?.let { ResponseEntity(it, HttpStatus.OK) }
            ?: ResponseEntity(HttpStatus.NOT_FOUND)

}
