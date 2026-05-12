package no.digdir.catalog_admin_service.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.*

@Entity
@Table(name = "catalog_users")
data class User(
    @Id
    @Column(name = "id")
    val id: String,

    @Column(name = "catalog_id", nullable = false)
    val catalogId: String,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "email")
    val email: String?,

    @Column(name = "telephone_number")
    val telephoneNumber: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserToBeCreated(
    val name: String,
    val email: String?,
    val telephoneNumber: String?
)
