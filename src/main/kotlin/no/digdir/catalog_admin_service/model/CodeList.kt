package no.digdir.catalog_admin_service.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "codeLists")
data class CodeList(
    @Id
    val id: String,
    val name: String,
    val catalogId: String,
    val description: String,
    val codes: List<Code>
)

data class CodeListToBeCreated(
    val name: String,
    val catalogId: String,
    val description: String,
    val codes: List<Code>
)
