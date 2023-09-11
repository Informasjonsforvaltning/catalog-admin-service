package no.digdir.catalog_admin_service.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "users")
data class User(
    @Id
    val id: String,
    val catalogId: String,
    val name: String,
    val email: String?,
    val telephoneNumber: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserToBeCreated(
    val name: String,
    val email: String?,
    val telephoneNumber: String?
)
