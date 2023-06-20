package no.digdir.catalog_admin_service.controller

import no.digdir.catalog_admin_service.model.DesignDBO
import no.digdir.catalog_admin_service.model.DesignDTO
import no.digdir.catalog_admin_service.model.Logo
import no.digdir.catalog_admin_service.security.EndpointPermissions
import no.digdir.catalog_admin_service.service.DesignService
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
import org.springframework.web.bind.annotation.ResponseBody


@Controller
@CrossOrigin
@RequestMapping(
    value = ["/{catalogId}/design"],
    produces = ["application/json"]
)
open class DesignController(
    private val designService: DesignService,
    private val endpointPermissions: EndpointPermissions
) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getDesign(@AuthenticationPrincipal jwt: Jwt, @PathVariable catalogId: String): ResponseEntity<DesignDTO> =
        if (endpointPermissions.hasOrgReadPermission(jwt, catalogId)) {
            designService.getDesign(catalogId)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateDesign(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @RequestBody designDTO: DesignDTO
    ): ResponseEntity<DesignDBO> = if (endpointPermissions.hasOrgAdminPermission(jwt, catalogId)) {
        val created = designService.updateDesign(catalogId, designDTO)
        ResponseEntity(
            created, HttpStatus.OK
        )
    } else ResponseEntity<DesignDBO>(HttpStatus.FORBIDDEN)

    @GetMapping(value = ["/logo"], produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    @ResponseBody
    fun getLogoFile(@AuthenticationPrincipal jwt: Jwt, @PathVariable catalogId: String): ResponseEntity<Logo> =
        if (endpointPermissions.hasOrgReadPermission(jwt, catalogId)) {
            ResponseEntity(designService.getLogo(catalogId), HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }

    @PostMapping(value = ["/logo"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateLogo(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @RequestBody logo: ByteArray
    ): ResponseEntity<Logo> = if (endpointPermissions.hasOrgAdminPermission(jwt, catalogId)) {
        val created = designService.saveLogo(catalogId, logo)
        ResponseEntity(
            created, HttpStatus.OK
        )
    } else ResponseEntity<Logo>(HttpStatus.FORBIDDEN)


    @DeleteMapping(value = ["/logo"])
    fun deleteLogo(
        @AuthenticationPrincipal jwt: Jwt, @PathVariable catalogId: String
    ): ResponseEntity<Logo> = when {
        !endpointPermissions.hasOrgAdminPermission(jwt, catalogId) -> ResponseEntity(HttpStatus.FORBIDDEN)
        designService.getLogo(catalogId) == null -> ResponseEntity(HttpStatus.NOT_FOUND)
        else -> {
            designService.deleteLogo(catalogId)
            ResponseEntity(HttpStatus.NO_CONTENT)
        }
    }
}
