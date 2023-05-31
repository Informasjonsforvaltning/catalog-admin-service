package no.digdir.catalog_admin_service.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class StatusController {
    @GetMapping("/ping")
    fun ping(): ResponseEntity<Void> =
        ResponseEntity.ok().build()

    @GetMapping("/ready")
    fun ready(): ResponseEntity<Void> =
        ResponseEntity.ok().build()
}