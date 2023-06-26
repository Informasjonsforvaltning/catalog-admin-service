package no.digdir.catalog_admin_service.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonValue
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

data class Fields(
    val editable: EditableFields,
    val internal: List<Field>
)

@Document(collection = "internalFields")
data class Field(
    @Id
    val id: String,
    val catalogId: String,
    val label: MultiLanguageTexts,
    val description: MultiLanguageTexts,
    val type: FieldType,
    val location: FieldLocation,
    val codeListId: String?
)

@Document(collection = "editableFields")
data class EditableFields(
    @Id
    val catalogId: String,
    val domainCodeListId: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FieldToBeCreated(
    val label: MultiLanguageTexts,
    val description: MultiLanguageTexts?,
    val type: FieldType?,
    val location: FieldLocation?,
    val codeListId: String?
)

enum class FieldType(private val value: String) {
    BOOLEAN("boolean"),
    TEXT_SHORT("text_short"),
    TEXT_LONG("text_long"),
    CODE_LIST("code_list"),
    USER_LIST("user_list");

    @JsonValue
    fun jsonValue(): String = value
}

enum class FieldLocation(private val value: String) {
    MAIN_COLUMN("main_column"),
    RIGHT_COLUMN("right_column");

    @JsonValue
    fun jsonValue(): String = value
}
