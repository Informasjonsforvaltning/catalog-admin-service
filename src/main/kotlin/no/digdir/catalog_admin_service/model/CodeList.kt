package no.digdir.catalog_admin_service.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "codeLists")
@CompoundIndexes(value = [CompoundIndex(name = "catalog_id", def = "{'catalogId' : 1}")])
data class CodeList(
    @Id
    val id: String,
    val name: String,
    val catalogId: String,
    val description: String,
    val codes: List<Code>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CodeListToBeCreated(
    val name: String,
    val description: String,
    val codes: List<Code>
)
