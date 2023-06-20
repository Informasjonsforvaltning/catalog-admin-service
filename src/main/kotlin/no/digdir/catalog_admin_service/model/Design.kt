package no.digdir.catalog_admin_service.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "design")
data class DesignDBO(
    @Id
    val catalogId: String,
    val backgroundColor: String?,
    val fontColor: String?,
    val logoDescription: String?
)

data class DesignDTO(
    val backgroundColor: String?,
    val fontColor: String?,
    val logoDescription: String?
)

@Document(collection = "logo")
data class Logo(
    @Id
    val catalogId: String,
    val logo: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Logo

        if (catalogId != other.catalogId) return false
        return logo.contentEquals(other.logo)
    }

    override fun hashCode(): Int {
        var result = catalogId.hashCode()
        result = 31 * result + logo.contentHashCode()
        return result
    }
}
