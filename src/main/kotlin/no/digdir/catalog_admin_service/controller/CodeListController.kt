package no.digdir.catalog_admin_service.controller

import no.digdir.catalog_admin_service.model.CodeList
import no.digdir.catalog_admin_service.model.CodeListToBeCreated
import no.digdir.catalog_admin_service.model.CodeLists
import no.digdir.catalog_admin_service.security.EndpointPermissions
import no.digdir.catalog_admin_service.service.CodeListService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@CrossOrigin
@RequestMapping(
    value = ["/{catalogId}/concepts/code-lists"],
    produces = ["application/json"]
)
open class CodeListController(
    private val codeListService: CodeListService,
    private val endpointPermissions: EndpointPermissions
) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getCodeLists(@AuthenticationPrincipal jwt: Jwt, @PathVariable catalogId: String): ResponseEntity<CodeLists> =
        if (endpointPermissions.hasOrgReadPermission(jwt, catalogId)) {
            ResponseEntity(codeListService.getCodeLists(catalogId), HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }


    @GetMapping(value = ["/{codeListId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getCatalogById(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @PathVariable codeListId: String
    ): ResponseEntity<CodeList> =
        if (endpointPermissions.hasOrgReadPermission(jwt, catalogId)) {
            codeListService.getCodeListById(catalogId, codeListId)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }

    @DeleteMapping(value = ["/{codeListId}"])
    fun deleteCodeList(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @PathVariable codeListId: String
    ): ResponseEntity<Unit> =
        if (endpointPermissions.hasOrgAdminPermission(jwt, catalogId) && codeListService.getCodeListById(catalogId, codeListId) != null) {
            codeListService.deleteCodeListById(codeListId)
            ResponseEntity(HttpStatus.NO_CONTENT)
        } else ResponseEntity(HttpStatus.FORBIDDEN)

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createCodeList(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @RequestBody newCodeList: CodeListToBeCreated
    ): ResponseEntity<Unit> =
        if (endpointPermissions.hasOrgAdminPermission(jwt, catalogId)) {
            val created = codeListService.createCodeList(newCodeList, catalogId)
            ResponseEntity(
                locationHeaderForCreated(newId = created.id, catalogId),
                HttpStatus.CREATED
            )
        } else ResponseEntity<Unit>(HttpStatus.FORBIDDEN)
}

private fun locationHeaderForCreated(newId: String, catalogId: String): HttpHeaders =
    HttpHeaders().apply {
        add(HttpHeaders.LOCATION, "/$catalogId/concepts/code-lists$newId")
        add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.LOCATION)
    }
