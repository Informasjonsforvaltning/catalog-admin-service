package no.digdir.catalogadmin.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "catalog_designs")
data class DesignDBO(
    @Id
    @Column(name = "catalog_id")
    val catalogId: String,
    @Column(name = "background_color")
    val backgroundColor: String?,
    @Column(name = "font_color")
    val fontColor: String?,
    @Column(name = "logo_description")
    val logoDescription: String?,
    @Column(name = "has_logo", nullable = false)
    val hasLogo: Boolean,
)

data class DesignDTO(val backgroundColor: String?, val fontColor: String?, val logoDescription: String?, val hasLogo: Boolean)

@Entity
@Table(name = "catalog_logos")
data class Logo(
    @Id
    @Column(name = "catalog_id")
    val catalogId: String,
    @Column(name = "content_type", nullable = false)
    val contentType: String,
    @Column(name = "base64_logo", columnDefinition = "text", nullable = false)
    val base64Logo: String,
    @Column(name = "filename", nullable = false)
    val filename: String,
)
