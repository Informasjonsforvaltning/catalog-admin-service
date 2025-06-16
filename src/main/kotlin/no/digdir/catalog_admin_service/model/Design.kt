package no.digdir.catalog_admin_service.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "catalogDesigns")
data class DesignDBO(
    @Id
    val catalogId: String,
    val backgroundColor: String?,
    val fontColor: String?,
    val logoDescription: String?,
    val hasLogo: Boolean = false
)

data class DesignDTO(
    val backgroundColor: String?,
    val fontColor: String?,
    val logoDescription: String?,
    val hasLogo: Boolean
)

@Document(collection = "catalogLogos")
data class Logo(
    @Id
    val catalogId: String,
    val contentType: String,
    val base64Logo: String,
    val filename: String
)
