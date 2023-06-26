package no.digdir.catalog_admin_service.controller

import no.digdir.catalog_admin_service.model.Fields
import no.digdir.catalog_admin_service.security.EndpointPermissions
import no.digdir.catalog_admin_service.service.FieldsService
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

}
