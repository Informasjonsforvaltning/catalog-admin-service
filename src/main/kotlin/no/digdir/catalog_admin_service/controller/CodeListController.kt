package no.digdir.catalog_admin_service.controller

import no.digdir.catalog_admin_service.model.CodeList
import no.digdir.catalog_admin_service.model.CodeLists
import no.digdir.catalog_admin_service.security.EndpointPermissions
import no.digdir.catalog_admin_service.service.CodeListService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@CrossOrigin
@RequestMapping(
    value = ["/catalogs/{catalogId}/concepts/code-lists"],
    produces = ["application/json"])
open class CodeListController(private val codeListService: CodeListService, private val endpointPermissions: EndpointPermissions) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getCodeLists(@AuthenticationPrincipal jwt: Jwt, @PathVariable catalogId: String): ResponseEntity<CodeLists> =
        if (endpointPermissions.hasOrgReadPermission(jwt, catalogId)) {
            ResponseEntity(codeListService.getCodeLists(catalogId), HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }


    @GetMapping(value = ["/{codeListId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getCatalogById(@AuthenticationPrincipal jwt: Jwt, @PathVariable catalogId: String, @PathVariable codeListId: String): ResponseEntity<CodeList> =
        if (endpointPermissions.hasOrgReadPermission(jwt, catalogId)) {
            codeListService.getCodeListById(catalogId, codeListId)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }

}
