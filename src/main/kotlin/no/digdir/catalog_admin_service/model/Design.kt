package no.digdir.catalog_admin_service.model

import jakarta.persistence.*

@Entity
@Table(name = "catalog_designs")
data class DesignDBO(
    @Id
    @Column(name = "catalog_id")
    val catalogId: String = "",

    @Column(name = "background_color")
    val backgroundColor: String? = null,

    @Column(name = "font_color")
    val fontColor: String? = null,

    @Column(name = "logo_description")
    val logoDescription: String? = null,

    @Column(name = "has_logo", nullable = false)
    val hasLogo: Boolean = false,
)

data class DesignDTO(
    val backgroundColor: String?,
    val fontColor: String?,
    val logoDescription: String?,
    val hasLogo: Boolean
)

@Entity
@Table(name = "catalog_logos")
data class Logo(
    @Id
    @Column(name = "catalog_id")
    val catalogId: String = "",

    @Column(name = "content_type", nullable = false)
    val contentType: String = "",

    @Column(name = "base64_logo", columnDefinition = "text", nullable = false)
    val base64Logo: String = "",

    @Column(name = "filename", nullable = false)
    val filename: String = "",
)
