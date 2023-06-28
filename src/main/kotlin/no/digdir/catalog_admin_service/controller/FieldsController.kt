package no.digdir.catalog_admin_service.controller

import no.digdir.catalog_admin_service.model.EditableFields
import no.digdir.catalog_admin_service.model.Field
import no.digdir.catalog_admin_service.model.FieldToBeCreated
import no.digdir.catalog_admin_service.model.Fields
import no.digdir.catalog_admin_service.security.EndpointPermissions
import no.digdir.catalog_admin_service.service.FieldsService
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

    @PostMapping(value = ["/editable"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateEditableFields(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @RequestBody editableFields: EditableFields
    ): ResponseEntity<EditableFields> =
        when {
            !endpointPermissions.hasOrgAdminPermission(jwt, catalogId) -> ResponseEntity(HttpStatus.FORBIDDEN)
            editableFields.catalogId != catalogId -> ResponseEntity(HttpStatus.BAD_REQUEST)
            else -> ResponseEntity(fieldsService.updateEditableFields(editableFields), HttpStatus.OK)
        }

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
                ResponseEntity(locationHeaderForCreated(created.id, catalogId), HttpStatus.OK)
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

}

private fun locationHeaderForCreated(newId: String, catalogId: String): HttpHeaders =
    HttpHeaders().apply {
        add(HttpHeaders.LOCATION, "/$catalogId/concepts/fields/internal/$newId")
        add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.LOCATION)
    }
