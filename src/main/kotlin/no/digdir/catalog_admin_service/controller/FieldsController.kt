package no.digdir.catalog_admin_service.controller

import no.digdir.catalog_admin_service.model.*
import no.digdir.catalog_admin_service.security.EndpointPermissions
import no.digdir.catalog_admin_service.service.FieldsService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(
    value = ["/{catalogId}/concepts/fields"],
    produces = ["application/json"]
)
class FieldsController(
    private val endpointPermissions: EndpointPermissions,
    private val fieldsService: FieldsService
) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getFields(@AuthenticationPrincipal jwt: Jwt, @PathVariable catalogId: String): ResponseEntity<Fields> =
        if (endpointPermissions.hasOrgReadPermission(jwt, catalogId)) {
            ResponseEntity(fieldsService.getCatalogFields(catalogId), HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }

    @PatchMapping(value = ["/editable"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun patchEditableFields(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @RequestBody patchOperations: List<JsonPatchOperation>
    ): ResponseEntity<EditableFields> =
        if (endpointPermissions.hasOrgAdminPermission(jwt, catalogId)) {
            ResponseEntity(fieldsService.updateEditableFields(catalogId, patchOperations), HttpStatus.OK)
        } else ResponseEntity(HttpStatus.FORBIDDEN)

    @PostMapping(value = ["/internal"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createInternalField(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @RequestBody field: FieldToBeCreated
    ): ResponseEntity<Unit> =
        when {
            !endpointPermissions.hasOrgAdminPermission(jwt, catalogId) -> ResponseEntity(HttpStatus.FORBIDDEN)
            else -> {
                val created = fieldsService.createInternalField(field, catalogId)
                ResponseEntity(locationHeaderForCreated(created.id, catalogId), HttpStatus.CREATED)
            }
        }

    @GetMapping(value = ["/internal/{fieldId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getInternalField(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @PathVariable fieldId: String
    ): ResponseEntity<Field> =
        if (endpointPermissions.hasOrgReadPermission(jwt, catalogId)) {
            fieldsService.getInternalField(fieldId, catalogId)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }

    @DeleteMapping(value = ["/internal/{fieldId}"])
    fun deleteInternalField(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @PathVariable fieldId: String
    ): ResponseEntity<Unit> =
        if (endpointPermissions.hasOrgAdminPermission(jwt, catalogId)) {
            fieldsService.deleteInternalField(fieldId, catalogId)
            ResponseEntity(HttpStatus.NO_CONTENT)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }

    @PatchMapping(value = ["/internal/{fieldId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun patchInternalField(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @PathVariable fieldId: String,
        @RequestBody patchOperations: List<JsonPatchOperation>
    ): ResponseEntity<Field> =
        if (endpointPermissions.hasOrgAdminPermission(jwt, catalogId)) {
            fieldsService.updateInternalField(fieldId, catalogId, patchOperations)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }

}

private fun locationHeaderForCreated(newId: String, catalogId: String): HttpHeaders =
    HttpHeaders().apply {
        add(HttpHeaders.LOCATION, "/$catalogId/concepts/fields/internal/$newId")
        add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.LOCATION)
    }
