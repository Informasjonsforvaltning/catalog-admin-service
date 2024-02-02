package no.digdir.catalog_admin_service.model

data class Code(
    val id: String,
    val name: MultiLanguageTexts,
    val parentID: String?
)
