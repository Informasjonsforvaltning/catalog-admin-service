package no.digdir.catalog_admin_service.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonValue
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

data class Fields(
    val editable: EditableFields,
    val internal: List<Field>
)

@Entity
@Table(name = "internal_fields")
data class Field(
    @Id
    @Column(name = "id")
    val id: String = "",

    @Column(name = "catalog_id", nullable = false)
    val catalogId: String = "",

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "label", columnDefinition = "jsonb", nullable = false)
    val label: MultiLanguageTexts = MultiLanguageTexts(null, null, null),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "description", columnDefinition = "jsonb", nullable = false)
    val description: MultiLanguageTexts = MultiLanguageTexts(null, null, null),

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: FieldType = FieldType.TEXT_SHORT,

    @Enumerated(EnumType.STRING)
    @Column(name = "location", nullable = false)
    val location: FieldLocation = FieldLocation.MAIN_COLUMN,

    @Column(name = "code_list_id")
    val codeListId: String? = null,

    @Column(name = "enable_filter")
    val enableFilter: Boolean? = null,
)

@Entity
@Table(name = "editable_fields")
data class EditableFields(
    @Id
    @Column(name = "catalog_id")
    val catalogId: String = "",

    @Column(name = "domain_code_list_id")
    val domainCodeListId: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FieldToBeCreated(
    val label: MultiLanguageTexts,
    val description: MultiLanguageTexts?,
    val type: FieldType?,
    val location: FieldLocation?,
    val codeListId: String?,
    val enableFilter: Boolean?
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
