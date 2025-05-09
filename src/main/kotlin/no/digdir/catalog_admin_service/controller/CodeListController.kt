package no.digdir.catalog_admin_service.controller

import no.digdir.catalog_admin_service.model.CodeList
import no.digdir.catalog_admin_service.model.CodeListToBeCreated
import no.digdir.catalog_admin_service.model.CodeLists
import no.digdir.catalog_admin_service.model.JsonPatchOperation
import no.digdir.catalog_admin_service.security.EndpointPermissions
import no.digdir.catalog_admin_service.service.CodeListService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

private val logger = LoggerFactory.getLogger(CodeListController::class.java)

@RestController
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
    fun getCodeListById(
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

    @PatchMapping(value = ["/{codeListId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun patchCodeList(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @PathVariable codeListId: String,
        @RequestBody patchOperations: List<JsonPatchOperation>
    ): ResponseEntity<CodeList> =
        if (endpointPermissions.hasOrgAdminPermission(jwt, catalogId)) {
            codeListService.updateCodeList(codeListId, catalogId, patchOperations)
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
        when {
            !endpointPermissions.hasOrgAdminPermission(jwt, catalogId) -> ResponseEntity(HttpStatus.FORBIDDEN)
            codeListService.getCodeListById(catalogId, codeListId) == null -> ResponseEntity(HttpStatus.NOT_FOUND)
            else -> {
                codeListService.deleteCodeListById(catalogId, codeListId)
                ResponseEntity(HttpStatus.NO_CONTENT)
            }
        }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createCodeList(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @RequestBody newCodeList: CodeListToBeCreated
    ): ResponseEntity<Unit> =
        if (endpointPermissions.hasOrgAdminPermission(jwt, catalogId)) {
            logger.info("creating codelist for ${catalogId}")
            val created = codeListService.createCodeList(newCodeList, catalogId)
            ResponseEntity(
                locationHeaderForCreated(newId = created.id, catalogId),
                HttpStatus.CREATED
            )

        } else ResponseEntity<Unit>(HttpStatus.FORBIDDEN)

    @PostMapping(
        value = ["/import"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun createListOfCodeLists(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @RequestBody codeLists: List<CodeListToBeCreated>
    ): ResponseEntity<Unit> {
        if (endpointPermissions.hasOrgAdminPermission(jwt, catalogId)) {
            logger.info("Creating ${codeLists.size} code lists for $catalogId")
            codeListService.createListOfCodeLists(codeLists, catalogId)
            return ResponseEntity(HttpStatus.CREATED)
        } else {
            return ResponseEntity<Unit>(HttpStatus.FORBIDDEN)
        }
    }
}

private fun locationHeaderForCreated(newId: String, catalogId: String): HttpHeaders =
    HttpHeaders().apply {
        add(HttpHeaders.LOCATION, "/$catalogId/concepts/code-lists/$newId")
        add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.LOCATION)
    }
