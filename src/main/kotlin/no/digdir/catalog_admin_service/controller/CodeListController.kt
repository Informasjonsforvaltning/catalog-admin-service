package no.digdir.catalog_admin_service.controller

import no.digdir.catalog_admin_service.model.CodeList
import no.digdir.catalog_admin_service.model.CodeLists
import no.digdir.catalog_admin_service.service.CodeListService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@CrossOrigin
@RequestMapping(
    value = ["/code-lists"],
    produces = ["application/json"])
open class CodeListController(private val codeListService: CodeListService) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getCodeLists(): ResponseEntity<CodeLists> {
        return ResponseEntity(codeListService.getCodeLists(), HttpStatus.OK)
    }

    @GetMapping(value = ["/{codeListId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getCatalogById(@PathVariable codeListId: String): ResponseEntity<CodeList> =
        codeListService.getCodeListById(codeListId)
            ?.let { ResponseEntity(it, HttpStatus.OK) }
            ?: ResponseEntity(HttpStatus.NOT_FOUND)
    }
