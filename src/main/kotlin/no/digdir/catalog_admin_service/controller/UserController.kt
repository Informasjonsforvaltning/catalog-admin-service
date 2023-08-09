package no.digdir.catalog_admin_service.controller

import no.digdir.catalog_admin_service.model.JsonPatchOperation
import no.digdir.catalog_admin_service.model.User
import no.digdir.catalog_admin_service.model.UserToBeCreated
import no.digdir.catalog_admin_service.model.Users
import no.digdir.catalog_admin_service.security.EndpointPermissions
import no.digdir.catalog_admin_service.service.UserService
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
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@CrossOrigin
@RequestMapping(
    value = ["/{catalogId}/general/users"],
    produces = ["application/json"]
)
open class UserController(
    private val userService: UserService,
    private val endpointPermissions: EndpointPermissions
) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getUsers(@AuthenticationPrincipal jwt: Jwt, @PathVariable catalogId: String): ResponseEntity<Users> =
        if (endpointPermissions.hasOrgReadPermission(jwt, catalogId)) {
            ResponseEntity(userService.getUsers(catalogId), HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }


    @GetMapping(value = ["/{userId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getUserById(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @PathVariable userId: String
    ): ResponseEntity<User> =
        if (endpointPermissions.hasOrgReadPermission(jwt, catalogId)) {
            userService.getUserById(userId, catalogId)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }

    @PatchMapping(value = ["/{userId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun patchUser(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @PathVariable userId: String,
        @RequestBody patchOperations: List<JsonPatchOperation>
    ): ResponseEntity<User> =
        if (endpointPermissions.hasOrgAdminPermission(jwt, catalogId)) {
            userService.updateUser(userId, catalogId, patchOperations)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }

    @DeleteMapping(value = ["/{userId}"])
    fun deleteUser(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @PathVariable userId: String
    ): ResponseEntity<Unit> =
        when {
            !endpointPermissions.hasOrgAdminPermission(jwt, catalogId) -> ResponseEntity(HttpStatus.FORBIDDEN)
            userService.getUserById(userId, catalogId) == null -> ResponseEntity(HttpStatus.NOT_FOUND)
            else -> {
                userService.deleteUserById(userId)
                ResponseEntity(HttpStatus.NO_CONTENT)
            }
        }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createUser(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @RequestBody newUser: UserToBeCreated
    ): ResponseEntity<Unit> =
        if (endpointPermissions.hasOrgAdminPermission(jwt, catalogId)) {
            val created = userService.createUser(newUser, catalogId)
            ResponseEntity(
                locationHeaderForCreated(newId = created.id, catalogId),
                HttpStatus.CREATED
            )

        } else ResponseEntity<Unit>(HttpStatus.FORBIDDEN)
}

private fun locationHeaderForCreated(newId: String, catalogId: String): HttpHeaders =
    HttpHeaders().apply {
        add(HttpHeaders.LOCATION, "/$catalogId/general/users/$newId")
        add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.LOCATION)
    }
