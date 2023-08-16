package no.digdir.catalog_admin_service.controller

import no.digdir.catalog_admin_service.model.DesignDTO
import no.digdir.catalog_admin_service.model.JsonPatchOperation
import no.digdir.catalog_admin_service.model.Logo
import no.digdir.catalog_admin_service.security.EndpointPermissions
import no.digdir.catalog_admin_service.service.DesignService
import no.digdir.catalog_admin_service.service.inputStreamResource
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import org.springframework.util.MimeType
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.multipart.MultipartFile


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
            ResponseEntity(designService.getDesign(catalogId), HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }

    @PatchMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateDesign(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @RequestBody patchOperations: List<JsonPatchOperation>
    ): ResponseEntity<DesignDTO> = if (endpointPermissions.hasOrgAdminPermission(jwt, catalogId)) {
        ResponseEntity(
            designService.updateDesign(catalogId, patchOperations), HttpStatus.OK
        )
    } else ResponseEntity<DesignDTO>(HttpStatus.FORBIDDEN)

    @GetMapping(value = ["/logo"])
    @ResponseBody
    fun getLogoFile(@AuthenticationPrincipal jwt: Jwt, @PathVariable catalogId: String): ResponseEntity<InputStreamResource> =
        if (endpointPermissions.hasOrgReadPermission(jwt, catalogId)) {
            val logo = designService.getLogo(catalogId)
            if (logo != null) ResponseEntity
                .ok()
                .contentType(MediaType.asMediaType(MimeType.valueOf(logo.contentType)))
                .headers(logo.fileNameHeader())
                .body(logo.inputStreamResource())
            else ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }

    @PostMapping(value = ["/logo"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun updateLogo(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @RequestPart("logo") logo: MultipartFile
    ): ResponseEntity<Logo> = if (endpointPermissions.hasOrgAdminPermission(jwt, catalogId)) {
        designService.saveLogo(catalogId, logo)
        ResponseEntity(HttpStatus.OK)
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

private fun Logo.fileNameHeader(): HttpHeaders =
    HttpHeaders().apply {
        add(HttpHeaders.CONTENT_DISPOSITION, """filename="$filename"""")
    }