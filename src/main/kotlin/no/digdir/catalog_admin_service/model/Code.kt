package no.digdir.catalog_admin_service.model

data class Code(
    val id: Int,
    val name: MultiLanguageTexts,
    val parentID: Int?
)
